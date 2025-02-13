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

/**
 * Exception to handle errors in the bus
 * 
 * @author Felix Wirth
 */
public class BusException extends Exception {

    /** SVID */
    private static final long serialVersionUID = 5462783962126095816L;
    
    /**
     * Creates a new instance
     * 
     * @param message
     */
    public BusException(String message) {
        super(message);
    }
    
    /**
     * Creates a new instance
     * 
     * @param message
     * @param cause
     */
    public BusException(String message, Throwable cause) {
        super(message, cause);
    }
}