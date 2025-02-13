/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bihealth.mi.easysmpc.cli;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bihealth.mi.easybus.Bus;
import org.bihealth.mi.easybus.ConnectionSettings;
import org.bihealth.mi.easybus.MessageListener;
import org.bihealth.mi.easybus.Participant;
import org.bihealth.mi.easybus.Scope;
import org.bihealth.mi.easysmpc.resources.Resources;

import de.tu_darmstadt.cbs.emailsmpc.Bin;
import de.tu_darmstadt.cbs.emailsmpc.Message;
import de.tu_darmstadt.cbs.emailsmpc.MessageInitial;

/**
 * A participant in an EasySMPC process
 * 
 * @author Felix Wirth
 * @author Fabian Prasser
 */
public class UserProcessParticipating extends UserProcess {

    /** Logger */
    private static final Logger LOGGER = LogManager.getLogger(UserProcessParticipating.class);
    /** User's values to add */
    private Map<String, String> data;

    /**
     * Creates a new instance
     * 
     * @param studyTitle
     * @param participant
     * @param data 
     * @param connectionSettings
     */
    public UserProcessParticipating(String studyTitle,
                                    Participant participant,
                                    Map<String, String> data,
                                    ConnectionSettings connectionSettings) {
        // Store
        super(connectionSettings);
        this.data = data;
        
        // Delete pre-existing bus messages
        // TODO Clarify purging!
        
        // Register for initial message
        Bus interimBus = getInterimBus();

        interimBus.receive(new Scope(studyTitle + Resources.ROUND_0), participant, new MessageListener() {
            boolean received = false;

            @Override
            public void receive(String message) {
                // Stop interim bus
                interimBus.stop();

                if (!received) {
                    // Spawns the following steps in an own thread
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            performInitialization(message);
                        }
                    });
                    thread.setDaemon(false);
                    thread.start();
                    received = true;
                }
            }

            @Override
            public void receiveError(Exception e) {
                LOGGER.error("Error receiveing messages", e);
            }
        });
    }

    /**
     * Receives the initial message
     * 
     * @param message
     */
    private void performInitialization(String message) {

        try {
            // Get data
            String data = Message.deserializeMessage(message).data;

            // Init model
            setModel(MessageInitial.getAppModel(MessageInitial.decodeMessage(Message.getMessageData(data))));
            getModel().setConnectionSettings(getConnectionSettings());

            // Proceed to entering value
            getModel().toEnteringValues(data);

            // Set own values and proceed
            getModel().toSendingShares(getValuesFromData());
            
            // Save state         
            save();

            // Starts the common steps 
            performCommonSteps();

        } catch (ClassNotFoundException | IllegalArgumentException | IllegalStateException | IOException e) {
            LOGGER.error("Unable to execute particpating users steps", e);
            throw new IllegalStateException("Unable to execute particpating users steps" , e);
        }        
    }
    
    /**
     * Creates the values as BigDecimal array by using the data in the field or the default value zero
     * 
     * @return
     */
    private BigDecimal[] getValuesFromData() {
        // Prepare
        BigDecimal[] values = new BigDecimal[getModel().getBins().length];
        
        // Loop over bins
        int i = 0;
        for (Bin bin : getModel().getBins()) {
            
            // Set either value from data if entry exits or zero
            if (this.data.get(bin.name) != null) {
                try {
                    values[i] = new BigDecimal(this.data.get(bin.name).trim().replace(',', '.'));
                } catch (NumberFormatException e) {
                    LOGGER.error(String.format("Unable to understand value %s for variable %s", data.get(bin.name)), bin.name);
                    values[i] = BigDecimal.ZERO;
                }
                
                this.data.remove(bin.name);
            } else {
                values[i] = BigDecimal.ZERO;
            }
            i++;
        }
        
        // Warning about unmapped variables
        for(Entry<String, String> entry : this.data.entrySet()) {
            LOGGER.warn(String.format("Data for variable \"%s\" was provided, but variable was not found in variable definition", entry.getKey()));
        }
        
        // Return
        return values;
    }
}