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
package org.bihealth.mi.easysmpc;

import org.bihealth.mi.easysmpc.resources.Resources;

/**
 * A perspective
 * 
 * @author Fabian Prasser
 * @author Felix Wirth
 */
public class Perspective5Receive extends Perspective3Receive {
    
    /**
     * Creates a new instance
     * @param app
     */
    protected Perspective5Receive(App app) {
        super(app, Resources.getString("PerspectiveReceive.receive2nd") , 5);
    }

    @Override
    protected void actionProceed() {
        getApp().actionSecondReceivingDone();
    }
    
    @Override
    protected String getRoundIdentifier() {
        return Resources.ROUND_2;
    }
}
