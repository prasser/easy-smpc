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

import java.awt.BorderLayout;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Displays a loading visualization and a respective text
 * 
 * @author Felix Wirth
 *
 */
public class ComponentLoadingVisual extends JPanel {

    /** SVUID */
    private static final long serialVersionUID = 6439753947234873179L;
    /** Image icon */
    private ImageIcon imageIcon;
    
    /**
     * Creates a new instance
     * @throws IOException 
     */
    public ComponentLoadingVisual(ImageIcon image) throws IOException{
        // Initialize
        this.imageIcon = image;
        
        // Create layout and add panel
        this.setLayout(new BorderLayout());
        
        // Deactivate as default
        this.deactivate();
    }
     
    /**
     * Sets the component to display loading is in progress
     */
    public void activate() {
        this.removeAll();
        this.add(new JLabel(this.imageIcon), BorderLayout.CENTER);
        this.revalidate();
        this.repaint();
    }
    
    /**
     * Sets the component to display loading is in progress
     */
    public void deactivate() {
        this.removeAll();
        this.revalidate();
        this.repaint();
    }
}