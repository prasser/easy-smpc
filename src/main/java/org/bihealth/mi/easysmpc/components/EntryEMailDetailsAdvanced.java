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
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;

import org.bihealth.mi.easybus.Participant;
import org.bihealth.mi.easybus.implementations.email.ConnectionSettingsIMAP;
import org.bihealth.mi.easysmpc.resources.Resources;

/**
 * Enter advanced e-mail details
 * 
 * @author Felix Wirth
 *
 */
public class EntryEMailDetailsAdvanced extends EntryEMailDetails {
    
    /** SVUID */
    private static final long         serialVersionUID = -7005095447195643542L;
    /** E-mail entry */
    private ComponentEntryOne         emailEntry;
    /** Password entry */
    private ComponentEntryOne         passwordEntry;
    /** user name entry */
    private ComponentEntryOneCheckBox userNameEntry;
    /** Auth mechanism entry */
    private ComponentEntryOneCheckBox authMechEntry;

    /**
     * Creates a new instance
     * 
     * @param title
     * @param standardPort
     */
    public EntryEMailDetailsAdvanced(String title, int standardPort) {
        // Super
        super(title, standardPort);
        
        // Create elements
        createAdditionalElements(title);
    }
    
    /**
     * Creates a new instance. If settings is not null fields are pre-filled from it
     * 
     * @param title
     * @param standardPort
     * @param settings
     * @param isIMAP
     * @param emailEnabled
     */
    EntryEMailDetailsAdvanced(String title,
                              int standardPort,
                              ConnectionSettingsIMAP settings,
                              boolean isIMAP,
                              boolean emailEnabled) {
        // Super
        super(title, standardPort, settings, isIMAP);
        
        // Create element
        createAdditionalElements(title);
        
        // Check
        if(settings == null) {
            return;
        }
        
        // Set values
        if (isIMAP) {

            // Set values from IMAP
            this.emailEntry.setValue(settings.getIMAPEmailAddress());
            this.passwordEntry.setValue(settings.getIMAPPassword(false));
            this.userNameEntry.setValue(settings.getIMAPUserName());
            this.authMechEntry.setValue(settings.getIMAPAuthMechanisms());
        } else {
            // Set values from SMTP
            this.emailEntry.setValue(settings.getSMTPEmailAddress());
            this.passwordEntry.setValue(settings.getSMTPPassword(false));
            this.userNameEntry.setValue(settings.getSMTPUserName());
            this.authMechEntry.setValue(settings.getSMTPAuthMechanisms());
        }
        
        // Moodify e-mail enabled
        this.emailEntry.setFieldEnabled(emailEnabled);
    }
    
    /**
     * Creates a new instance. If oldDetails is not null fields are pre-filled from it
     * 
     * @param title
     * @param standardPort
     * @param oldDetails
     */
    public EntryEMailDetailsAdvanced(String title, int standardPort, EntryEMailDetails oldDetails) {
        // Super
        super(title, standardPort, oldDetails);
        
        // Create element
        createAdditionalElements(title);
        
        // Check
        if(oldDetails == null) {
            return;
        }
        
        this.emailEntry.setValue(oldDetails.getEmailAddress());
        this.passwordEntry.setValue(oldDetails.getPassword());
        this.userNameEntry.setValue(oldDetails.getUserName());
        this.authMechEntry.setValue(oldDetails.getAuthMechanisms());
        
    }
    
    /**
     * Creates a new instance. If oldDetails is not null fields are pre-filled from it. Finally the parameters emailAddress and password overwrite the values from oldDetails again if not null
     * 
     * @param title
     * @param standardPort
     * @param oldDetails
     * @param eMailAddress
     * @param password
     * @param emailEnabled
     */
    public EntryEMailDetailsAdvanced(String title, int standardPort, EntryEMailDetails oldDetails, String emailAddress, String password, boolean emailEnabled) {
        this(title, standardPort, oldDetails);
        
        // Overwrite values
        if(emailAddress != null) emailEntry.setValue(emailAddress);
        if(password != null) passwordEntry.setValue(password);
        emailEntry.setFieldEnabled(emailEnabled);        
    }

    /**
     * Creates the additional elements
     * 
     * @param title
     */
    private void createAdditionalElements(String title) {
        
        // Reset
        this.removeAll();
        this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                        title,
                                                        TitledBorder.LEFT,
                                                        TitledBorder.DEFAULT_POSITION));
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        // E-mail address entry
        emailEntry = new ComponentEntryOne(Resources.getString("EmailConfig.1"),
        "",
        true,
        new ComponentTextFieldValidator() {
              @Override
              public boolean validate(String text) {
                  return Participant.isEmailValid(text);
              }
          }, false, false);
        this.add(emailEntry);
        
        // Password entry
        passwordEntry = new ComponentEntryOne(Resources.getString("EmailConfig.2"),
                                              null,
                                              true,
                                              new ComponentTextFieldValidator() {

                                                  @Override
                                                  public boolean validate(String text) {
                                                      return text != null && !text.isBlank();
                                                  }
                                              },
                                              true,
                                              false);

        // User name entry
        userNameEntry = new ComponentEntryOneCheckBox(Resources.getString("EmailConfig.28"),
                                                      new ComponentTextFieldValidator() {

                                                          @Override
                                                          public boolean validate(String text) {
                                                              return text != null &&
                                                                     !text.isBlank();
                                                          }
                                                      });
        
        // Auth mechanism entry
        authMechEntry = new ComponentEntryOneCheckBox(Resources.getString("EmailConfig.30"),
                                                      new ComponentTextFieldValidator() {

                                                          @Override
                                                          public boolean validate(String text) {
                                                              return text != null &&
                                                                     !text.isBlank();
                                                          }
                                                      });
        
        // Add
        this.add(emailEntry);
        this.add(userNameEntry);
        this.add(passwordEntry);
        this.add(getServerEntry());
        this.add(getPortEntry());
        this.add(authMechEntry);
        this.add(getRadioEncryptionType());
    }
    
    /**
     *  Returns whether the settings are valid
     * 
     * @return
     */
    @Override
    public boolean areValuesValid() {
        return super.areValuesValid() 
                && emailEntry.isValueValid()
                && userNameEntry.isValueValid()
                && passwordEntry.isValueValid()
                && authMechEntry.isValueValid();
    }
    
    /**
     * Sets a change listener
     * @param listener
     */
    @Override
    public void setChangeListener(ChangeListener listener) {
        super.setChangeListener(listener);
        this.emailEntry.setChangeListener(listener);
        this.userNameEntry.setChangeListener(listener);
        this.passwordEntry.setChangeListener(listener);
        this.authMechEntry.setChangeListener(listener);
    }
    
    /**
     * Returns e-mail address
     * 
     * @return
     */
    @Override
    public String getEmailAddress() {
        return emailEntry.getValue();
    }

    /**
     * Return password
     * 
     * @return
     */
    @Override
    public String getPassword() {
        return passwordEntry.getValue();
    }    
    
    /**
     * Get user name
     * 
     * @return
     */
    @Override
    public String getUserName() {
        return userNameEntry.getValue() == null || userNameEntry.getValue().isBlank() ? null : userNameEntry.getValue();
    }

    /**
     * Get auth mechansism
     * 
     * @return
     */
    @Override
    public String getAuthMechanisms() {
        return authMechEntry.getValue() == null || authMechEntry.getValue().isBlank() ? null : authMechEntry.getValue();
    }
}
