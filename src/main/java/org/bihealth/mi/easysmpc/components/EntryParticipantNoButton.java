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

import javax.swing.JPanel;

/**
 * Entry for participants
 * 
 * @author Fabian Prasser
 * @author Felix Wirth
 */
public class EntryParticipantNoButton extends EntryParticipant {
    
    /** SVID*/
    private static final long serialVersionUID = -8287188327564633383L;

    /**
     * Creates a new instance
     * @param name
     * @param value
     */
    public EntryParticipantNoButton(String name, String value, boolean isOwnEntry){
        super(name, value, false, false, isOwnEntry);
    }

    /**
     * Creates and additional control panel
     */
    @Override
    protected JPanel createAdditionalControls() {
        return null;
    }
    
}
