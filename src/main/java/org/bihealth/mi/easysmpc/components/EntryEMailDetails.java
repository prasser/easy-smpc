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
package org.bihealth.mi.easysmpc.components;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;

import org.bihealth.mi.easybus.implementations.email.ConnectionSettingsIMAP;
import org.bihealth.mi.easysmpc.resources.Resources;

/**
 * Entry to enter the followng e-mail details: server name, port, host
 * For user name, password etc. please use EntryEMailDetailsAdvanced
 * 
 * @author Felix Wirth
 *
 */
public class EntryEMailDetails extends JPanel {

    /** SVUID */
    private static final long serialVersionUID = 3375406542374186905L;
    /** Server entry */
    private final ComponentEntryOne serverEntry;
    /** Port entry */
    private final ComponentEntryOne portEntry;
    /** Encryption type radio */
    private final ComponentRadioEntry radioEncryptionType;
    
    /**
     * Creates a new instance
     * 
     * @param title
     * @param standardPort
     */
    public EntryEMailDetails(String title, int standardPort) {
        
        // General
        this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                        title,
                                                        TitledBorder.LEFT,
                                                        TitledBorder.DEFAULT_POSITION));
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        // Server entry
        serverEntry = new ComponentEntryOne(Resources.getString("EmailConfig.39"),
        "",
        true,
        new ComponentTextFieldValidator() {
              @Override
              public boolean validate(String text) {
                  return text != null && !text.isBlank();
              }
          }, false, false);
        this.add(serverEntry);
        
        // Port entry
        portEntry = new ComponentEntryOne(Resources.getString("EmailConfig.40"),
                                                            String.valueOf(standardPort),
                                                              true,
                                                              new ComponentTextFieldValidator() {
                                                                    @Override
                                                                    public boolean validate(String text) {
                                                                        try {
                                                                            ConnectionSettingsIMAP.checkPort(Integer.parseInt(text));
                                                                            return true;
                                                                        }
                                                                        catch (Exception e) {
                                                                            return false;
                                                                        }
                                                                    }
                                                                }, false, false);
        this.add(portEntry);
        
        // Encryption entry
        radioEncryptionType = new ComponentRadioEntry(Resources.getString("EmailConfig.20"),
                                                      Resources.getString("EmailConfig.21"),
                                                      Resources.getString("EmailConfig.22"));
        //radioEncryptionType.setChangeListener(this);
        this.add(radioEncryptionType);
    }

    /**
     * Creates a new instance. If settings is not null fields are pre-filled from it
     * 
     * @param title
     * @param defaultPortImap
     * @param settings
     * @param isIMAP
     */
    public EntryEMailDetails(String title, int defaultPort, ConnectionSettingsIMAP settings, boolean isIMAP) {
        this(title, defaultPort);       
        
        // Check
        if(settings == null) {
            return;
        }
        
        // Set values
        if (isIMAP) {
            this.serverEntry.setValue(settings.getIMAPServer());
            this.portEntry.setValue(Integer.toString(settings.getIMAPPort()));
            this.radioEncryptionType.setFirstOptionSelected(settings.isSSLTLSIMAP());
        } else {
            this.serverEntry.setValue(settings.getSMTPServer());
            this.portEntry.setValue(Integer.toString(settings.getSMTPPort()));
            this.radioEncryptionType.setFirstOptionSelected(settings.isSSLTLSSMTP());
        }
    }    

    /**
     * Creates a new instance. If oldDetails is not null fields are pre-filled from it
     * 
     * @param title
     * @param defaultPort
     * @param oldDetails
     */
    public EntryEMailDetails(String title, int defaultPort, EntryEMailDetails oldDetails) {
        this(title, defaultPort);
        
        // Check
        if(oldDetails == null) {
            return;
        }
        
        // Set values
        this.serverEntry.setValue(oldDetails.getServerEntry().getValue());
        this.portEntry.setValue(oldDetails.getPortEntry().getValue());
        this.radioEncryptionType.setFirstOptionSelected(oldDetails.getRadioEncryptionType().isFirstOptionSelected());
    }

    /**
     * @return the serverEntry
     */
    protected ComponentEntryOne getServerEntry() {
        return serverEntry;
    }

    /**
     * @return the portEntry
     */
    protected ComponentEntryOne getPortEntry() {
        return portEntry;
    }

    /**
     * @return the radioEncryption
     */
    protected ComponentRadioEntry getRadioEncryptionType() {
        return radioEncryptionType;
    }
    
    /**
     * Sets a change listener
     * @param listener
     */
    public void setChangeListener(ChangeListener listener) {
        this.portEntry.setChangeListener(listener);
        this.serverEntry.setChangeListener(listener);
        this.radioEncryptionType.setChangeListener(listener);
    }
        
    /**
     *  Returns whether the settings are valid
     * 
     * @return
     */
    public boolean areValuesValid() {
        return serverEntry.isValueValid() && portEntry.isValueValid();          
    }
    
    /**
     * Get e-mail address
     * 
     * @return
     */
    public String getEmailAddress() {
        return null;
    }

    /**
     * Get password
     * 
     * @return
     */
    public String getPassword() {
        return null;
    }    
    
    /**
     * Get user name
     * 
     * @return
     */
    public String getUserName() {
        return null;
    }

    /**
     * Get auth mechansism
     * 
     * @return
     */
    public String getAuthMechanisms() {
        return null;
    }
    
    /**
     * Get server
     * 
     * @return
     */
    public String getServer() {
        return this.serverEntry.getValue();
    }
    
    /**
     * Get server port
     * 
     * @return
     */
    public int getPort() {
        return portEntry.isValueValid() ? Integer.valueOf(portEntry.getValue()) : 0;
    }
    
    /**
     * Is SSL/TLS set?
     */
    public boolean isSSLTLS() {
        return radioEncryptionType.isFirstOptionSelected();
    }    
}
