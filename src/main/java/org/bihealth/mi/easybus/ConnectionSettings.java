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
package org.bihealth.mi.easybus;

import java.io.Serializable;

import org.bihealth.mi.easysmpc.resources.Resources;

/** Generic settings
 * 
 * @author Felix Wirth
 *
 */
public abstract class ConnectionSettings implements Serializable {
    
    /** SVUID */
    private static final long serialVersionUID = -3887172032343688839L;
    /** Password store */
    private transient PasswordStore passwordStore;
    
    /**
     * Returns the identifier
     * @return
     */
    public abstract String getIdentifier();
    
    /**
     * Returns whether this connection is valid
     * @param usePasswordProvider
     * @return
     */
    public abstract boolean isValid(boolean usePasswordProvider);
    
    /**
     * Return the check interval
     * 
     * @return
     */
    public abstract int getCheckInterval();
    
    /**
     * Get send timeout
     * 
     * @return
     */
    public abstract int getSendTimeout();
    
    /**
     * @return the passwordStore
     */
    public PasswordStore getPasswordStore() {
        return passwordStore;
    }
    
    /**
     * @return the maxMessageSize
     */
    public abstract int getMaxMessageSize();
    
    /**
     * @param passwordStore the passwordStore to set
     */
    public ConnectionSettings setPasswordStore(PasswordStore passwordStore) {
        this.passwordStore = passwordStore;
        return this;
    }
    
    /**
     * Returns the exchange mode
     * 
     * @return
     */
    public abstract ExchangeMode getExchangeMode();
    
    /** Connection types */
    public enum ExchangeMode { 
        /** Enum values */
        MANUAL, EMAIL, EASYBACKEND;
        
        public String toString() {
            return Resources.getString(String.format("ExchangeMode.%s", this.name()));
        }
    }
}