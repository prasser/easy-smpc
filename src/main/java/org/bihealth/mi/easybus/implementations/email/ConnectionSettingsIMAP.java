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
package org.bihealth.mi.easybus.implementations.email;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProxySelector;
import java.net.URL;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;

import org.bihealth.mi.easybus.ConnectionSettings;
import org.bihealth.mi.easybus.Participant;
import org.bihealth.mi.easybus.PasswordStore;
import org.bihealth.mi.easybus.PerformanceListener;
import org.bihealth.mi.easybus.implementations.PasswordProvider;
import org.bihealth.mi.easysmpc.resources.Resources;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.github.markusbernhardt.proxy.ProxySearch;

/**
 * Settings for IMAP connections
 * 
 * @author Felix Wirth
 * @author Fabian Prasser
 */
public class ConnectionSettingsIMAP extends ConnectionSettings {

    /** SVUID */
    private static final long    serialVersionUID            = 3880443185633907293L;
    /** End-point for Mozilla auto-configuration service */
    public static final String   MOZILLA_AUTOCONF            = "https://autoconfig.thunderbird.net/v1.1/";
    /** Standard port for IMAP */
    public static final int      DEFAULT_PORT_IMAP           = 993;
    /** Standard port for SMTP */
    public static final int      DEFAULT_PORT_SMTP           = 465;
    /** Regex to check dns validity */
    private static final Pattern regexDNS                    = Pattern.compile("^([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])(\\.([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9]))*$");
    /** Key */
    private static final String  EMAIL_ADDRESS_KEY           = "email_address";
    /** Key */
    private static final String  IMAP_SERVER_KEY             = "imap_server";
    /** Key */
    private static final String  IMAP_PORT_KEY               = "imap_port";
    /** Key */
    private static final String  IMAP_ENCRYPTION_TYPE        = "imap_encryption";
    /** Key */
    private static final String  SMTP_SERVER_KEY             = "smtp_server";
    /** Key */
    private static final String  SMTP_PORT_KEY               = "smtp_port";
    /** Key */
    private static final String  SMTP_ENCRYPTION_TYPE        = "smtp_encryption";
    /** Key */
    private static final String  ACCEPT_SELF_SIGNED_CERT_KEY = "accept_self_signed_cert";
    /** Key */
    private static final String  USE_PROXY_KEY               = "use_poxy";
    /** Encryption ssl/tls */
    private static final String  SSL_TLS                     = "SSLTLS";
    /** Encryption starttls */
    private static final String  START_TLS                   = "STARTTLS";

    /**
     * Check server name
     * 
     * @param text
     */
    public static void checkDNSName(String text) {
        if (!regexDNS.matcher(text).matches()) {
            throw new IllegalArgumentException("DNS name invalid");
        }
    }

    /**
     * Check
     * 
     * @param object
     */
    public static void checkPort(int object) {
        if (object < 1 || object > 65535) {
            throw new IllegalArgumentException("Port must not be between 1 and 65535");
        }
    }

    /** E-mail address */
    private String                        imapEmailAddress;
    /** IMAP server dns */
    private String                        imapServer;
    /** Port of IMAP server */
    private int                           imapPort             = DEFAULT_PORT_IMAP;
    /** SMTP server dns */
    private String                        smtpServer;
    /** Port of SMTP server */
    private int                           smtpPort             = DEFAULT_PORT_SMTP;
    /** Accept self signed certificates */
    private boolean                       acceptSelfSignedCert = false;
    /** Search for proxy */
    private boolean                       searchForProxy       = false;
    /** Performance listener */
    private transient PerformanceListener listener             = null;
    /** Use ssl/tls (=true) or starttls (=false) for IMAP connection */
    private boolean                       ssltlsIMAP           = true;
    /** Use ssl/tls (=true) or starttls (=false) for SMTP connection */
    private boolean                       ssltlsSMTP           = true;
    /** Password accessor for receiving */
    private PasswordProvider              provider;
    /** E-mail address sending */
    private String                        smptEmailAddress;
    /** User name for IMAP */
    private String                        imapUserName         = null;
    /** User name for SMTP */
    private String                        smtpUserName         = null;
    /** Auth mechanisms for IMAP */
    private String                        imapAuthMechanisms   = null;
    /** Auth mechanisms for SMTP */
    private String                        smtpAuthMechanisms   = null;
    /** Maximal e-mail message size in bytes */
    private int                           maxMessageSize;
    /** E-mail check interval in milliseconds */
    private int                           checkInterval;
    /** E-mail send timeout in milliseconds */
    private int                           emailSendTimeout;

    /**
     * Creates a new instance with same mail address for sending and receiving
     * 
     * @param emailAddress
     * @param provider
     */
    public ConnectionSettingsIMAP(String emailAddress, PasswordProvider provider) {
        
        this(emailAddress, emailAddress, provider);
    }    
    
    /**
     * Creates a new instance
     * 
     * @param emailAddressIMAP
     * @param emailAddressSMPT
     * @param provider
     */
    public ConnectionSettingsIMAP(String emailAddressIMAP,
                                  String emailAddressSMTP,
                                  PasswordProvider provider) {
        // Checks
        checkNonNull(emailAddressIMAP);
        checkNonNull(emailAddressSMTP);
        if (!Participant.isEmailValid(emailAddressIMAP)) {
            throw new IllegalArgumentException("Invalid e-mail address for IMAP");
        }
        if (!Participant.isEmailValid(emailAddressSMTP)) {
            throw new IllegalArgumentException("Invalid e-mail address for SMTP");
        }      
        
        // Store
        this.imapEmailAddress = emailAddressIMAP;
        this.provider = provider;
        this.smptEmailAddress = emailAddressSMTP;
    }
    
    /**
     * Checks whether fields are empty
     * 
     * @return has null fields
     */
    public void check() {
        if (this.imapEmailAddress == null || this.imapServer == null || this.smtpServer == null) {
            throw new IllegalArgumentException("Connection parameters must not be null");
        }
    }

    /**
     * Return config parameter
     * @return the IMAP emailAddress
     */
    public String getIMAPEmailAddress() {
        return imapEmailAddress;
    }
    
    /**
     * Return config parameter
     * @return the SMTP emailAddress
     */
    public String getSMTPEmailAddress() {
        return smptEmailAddress;
    }

    /**
     * Return config parameter
     * @return the imapPort
     */
    public int getIMAPPort() {
        return imapPort;
    }

    /**
     * Return config parameter
     * @return the imapServer
     */
    public String getIMAPServer() {
        return imapServer;
    }
    
    /**
     * Return config parameter
     * @return the IMAP password
     */
    public String getIMAPPassword() {
        return getIMAPPassword(true);
    }
    
    /**
     * Return config parameter
     * @return the SMTP password
     */
    public String getSMTPPassword() {
        return getSMTPPassword(true);
    }

    /**
     * Return config parameter
     * @param usePasswordProvider
     * @return the password
     */
    public String getIMAPPassword(boolean usePasswordProvider) {
        
        // Potentially ask for password
        if ((this.getPasswordStore() == null || this.getPasswordStore().getFirstPassword() == null) && this.provider != null && usePasswordProvider) {
            // Get passwords
            PasswordStore store = this.provider.getPassword();
            
            // Check
            if(store == null) {
                return null;
            }
            
            // Store
            this.setPasswordStore(store);
            
            // Check connection settings
            if (!this.isValid(false)) {
                setPasswordStore(null);
            }
        }
        
        // Return password
        return getPasswordStore() == null ? null : getPasswordStore().getFirstPassword();
    }
    
    /**
     * Return config parameter
     * @param usePasswordProvider
     * @return the SMTP password
     */
    public String getSMTPPassword(boolean usePasswordProvider) {
        
        // Potentially ask for password
        if ((this.getPasswordStore() == null || this.getPasswordStore().getSecondPassword() == null) && this.provider != null && usePasswordProvider) {
            // Get passwords
            PasswordStore store = this.provider.getPassword();
            
            // Check
            if(store == null) {
                return null;
            }
            
            // Store password
            this.setPasswordStore(store);
            
            // Check connection settings
            if (!this.isValid(false)) {
                setPasswordStore(null);
            }
        }
        
        // Return password
        return getPasswordStore() == null ? null : getPasswordStore().getSecondPassword();
    }

    /**
     * Returns performance listener
     * @return
     */
	public PerformanceListener getPerformanceListener() {
		return listener;
	}

    /**
     * Return config parameter
     * @return the smtpPort
     */
    public int getSMTPPort() {
        return smtpPort;
    }
    
    /**
     * Return config parameter
     * @return the smtpServer
     */
    public String getSMTPServer() {
        return smtpServer;
    }
    
    /**
     * Return SMTP user name
     * 
     * @return
     */
    public String getSMTPUserName() {
        return smtpUserName;
    }
    
    /**
     * Return IMAP user name
     * 
     * @return
     */
    public String getIMAPUserName() {
        return imapUserName;
    }
    
    /**
     * Return IMAP auth mechanisms
     * 
     * @see "mail.imap.auth.mechanisms" at <a href="https://jakarta.ee/specifications/mail/1.6/apidocs/com/sun/mail/imap/package-summary.html"> Jakarta mail doc </a>
     * 
     * @return
     */
    public String getIMAPAuthMechanisms() {
        return imapAuthMechanisms;
    }
    
    /**
     * Return SMTP auth mechanisms
     *
     * @see "mail.smtp.auth.mechanisms" at <a href="https://jakarta.ee/specifications/mail/1.6/apidocs/com/sun/mail/smtp/package-summary.html">Jakarta mail doc </a>
     * 
     * @return
     */
    public String getSMTPAuthMechanisms() {
        return smtpAuthMechanisms;
    }

    /**
     * @return the emailSendTimeout
     */
    @Override
    public int getSendTimeout() {
        return emailSendTimeout;
    }
    
    /**
     * @return the maxMessageSize
     */
    public int getMaxMessageSize() {
        return maxMessageSize > 0 ? maxMessageSize : Resources.EMAIL_MAX_MESSAGE_SIZE_DEFAULT;
    }
    
    /**
     * @return the checkInterval
     */
    @Override
    public int getCheckInterval() {
        return checkInterval;
    }

    /**
     * Tries to guess the connection settings from the email address provider
     * 
     * @param Whether settings could be guessed successfully
     */
    public boolean guess() {
        // Auto discovery for proxy connections
        ProxySelector.setDefault(ProxySearch.getDefaultProxySearch().getProxySelector());
        
        // Initialize
        String mozillaConfEndpoint = MOZILLA_AUTOCONF + imapEmailAddress.substring(imapEmailAddress.indexOf("@") + 1, imapEmailAddress.length());

        try {
            
            // Request and serialize XML document
            HttpURLConnection connection = (HttpURLConnection) new URL(mozillaConfEndpoint).openConnection();
            connection.setRequestMethod("GET");
            Document doc = DocumentBuilderFactory.newInstance()
                                        .newDocumentBuilder()
                                        .parse(connection.getInputStream());

            // Set IMAP
            NodeList list = doc.getDocumentElement().getElementsByTagName("incomingServer");
            for (int i = 0; i < list.getLength(); i++) {
                Element element = (Element) list.item(i);
                if (element.getAttributes()
                           .getNamedItem("type")
                           .getNodeValue()
                           .equalsIgnoreCase("imap")) {
                    this.imapServer = element.getElementsByTagName("hostname")
                                             .item(0)
                                             .getChildNodes()
                                             .item(0)
                                             .getNodeValue();
                    this.imapPort = Integer.parseInt(element.getElementsByTagName("port")
                                                            .item(0)
                                                            .getChildNodes()
                                                            .item(0)
                                                            .getNodeValue());
                    this.ssltlsIMAP = element.getElementsByTagName("socketType")
                                             .item(0)
                                             .getChildNodes()
                                             .item(0)
                                             .getNodeValue()
                                             .equals("STARTTLS") ? false : true;
                }
            }

            // Set SMTP
            Element element = (Element) doc.getDocumentElement()
                                   .getElementsByTagName("outgoingServer")
                                   .item(0);
            smtpServer = element.getElementsByTagName("hostname")
                                .item(0)
                                .getChildNodes()
                                .item(0)
                                .getNodeValue();
            this.smtpPort = Integer.parseInt(element.getElementsByTagName("port")
                                                    .item(0)
                                                    .getChildNodes()
                                                    .item(0)
                                                    .getNodeValue());
            this.ssltlsSMTP = element.getElementsByTagName("socketType")
                    .item(0)
                    .getChildNodes()
                    .item(0)
                    .getNodeValue()
                    .equals("STARTTLS") ? false : true;
            
            // Success
            return true;
            
        } catch (Exception e) {
            // No success
            return false;
        }
    }
      
    /**
     * Returns whether self-signed certificates are accepted
     * @return
     */
    public boolean isAcceptSelfSignedCertificates() {
    	return acceptSelfSignedCert;
    }

    /**
     * Search for proxy
     * @return
     */
	public boolean isSearchForProxy() {
		return this.searchForProxy;
	}

    /**
     * Is ssl/tls or startls used for IMAP connection?
     * @return the ssltlsIMAP
     */
    public boolean isSSLTLSIMAP() {
        return ssltlsIMAP;
    }

    /**
     * Is ssl/tls or startls used for SMTP connection?     
     * @return the ssltlsSMTP
     */
    public boolean isSSLTLSSMTP() {
        return ssltlsSMTP;
    }

    /**
     * Returns whether this connection is valid
     * 
     * @param usePasswordProvider
     * @return
     */
    @Override
    public boolean isValid(boolean usePasswordProvider) {
        
        if ((this.getPasswordStore() == null || this.getPasswordStore().getFirstPassword() == null) && !usePasswordProvider) {
            return false;
        }

        try {
            return new ConnectionIMAP(this, false).checkConnection();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Accept self-signed certificates
     * @param accept
     */
    public ConnectionSettingsIMAP setAcceptSelfSignedCertificates(boolean accept) {
    	this.acceptSelfSignedCert = accept;
    	return this;
    }
    
    /**
     * Set config parameter
     * @param imapPort the IMAP port to set
     */
    public ConnectionSettingsIMAP setIMAPPort(int imapPort) {

        // Check
        checkNonNull(imapPort);
        checkPort(imapPort);
        
        // Set
        this.imapPort = imapPort;

        // Done
        return this;
    }
    
    /**
     * Set config parameter
     * @param imapServer the IMAP server to set
     */
    public ConnectionSettingsIMAP setIMAPServer(String imapServer) {
        
        // Check
        checkNonNull(imapServer);
        checkDNSName(imapServer);
        
        // Set
        this.imapServer = imapServer;

        // Done
        return this;
    }
    
    /**
     * Set IMAP user name
     * 
     * @param imapUserName
     * @return
     */
    public ConnectionSettingsIMAP setIMAPUserName(String imapUserName) {
        this.imapUserName = imapUserName;
        return this;
    }
    
    /**
     * Set SMTP user name
     * 
     * @param smtpUserName
     * @return
     */
    public ConnectionSettingsIMAP setSMTPUserName(String smtpUserName) {
        this.smtpUserName = smtpUserName;
        return this;
    }
    
    /**
     * Sets performance listener
     * @param listener
     * @return
     */
    public ConnectionSettingsIMAP setPerformanceListener(PerformanceListener listener) {
    	this.listener = listener;
    	return this;
    }
    
    /**
     * Search for proxy
     * @param search
     * @return
     */
    public ConnectionSettingsIMAP setSearchForProxy(boolean search) {
    	this.searchForProxy = search;
    	return this;
    }
    
    /**
     * Set config parameter
     * @param smtpPort the SMTP port to set
     */
    public ConnectionSettingsIMAP setSMTPPort(int smtpPort) {

        // Check
        checkNonNull(smtpPort);
        checkPort(smtpPort);
        
        // Set
        this.smtpPort = smtpPort;

        // Done
        return this;
    }

    /**
     * Set config parameter
     * @param smtpServer the SMTP server to set
     */
    public ConnectionSettingsIMAP setSMTPServer(String smtpServer) {

        // Check
        checkNonNull(smtpServer);
        checkDNSName(smtpServer);
        
        // Set
        this.smtpServer = smtpServer;

        // Done
        return this;
    }
    
    /**
     * @param ssltlsIMAP the ssltlsIMAP to set
     */
    public ConnectionSettingsIMAP setSSLTLSIMAP(boolean ssltlsIMAP) {
        // Set
        this.ssltlsIMAP = ssltlsIMAP;
        
        // Done
        return this;
    }
    /**
     * @param ssltlsSMTP the ssltlsSMTP to set
     */
    public ConnectionSettingsIMAP setSSLTLSSMTP(boolean ssltlsSMTP) {
        // Set
        this.ssltlsSMTP = ssltlsSMTP;
        
        // Done
        return this;
    }
    
    /**
     * Set IMAP auth mechanisms
     * @see "mail.imap.auth.mechanisms" at <a href="https://jakarta.ee/specifications/mail/1.6/apidocs/com/sun/mail/imap/package-summary.html"> Jakarta mail doc </a>
     * 
     * @return
     */
    public ConnectionSettingsIMAP setIMAPAuthMechanisms(String imapAuthMechanisms) {
        this.imapAuthMechanisms = imapAuthMechanisms;
        return this;
    }
    
    /**
     * Set SMTP auth mechanisms
     *
     * @see "mail.smtp.auth.mechanisms" at <a href="https://jakarta.ee/specifications/mail/1.6/apidocs/com/sun/mail/smtp/package-summary.html"> Jakarta mail doc </a>
     * 
     * @return
     */
    public ConnectionSettingsIMAP setSMTPAuthMechanisms(String smtpAuthMechanisms) {
        this.smtpAuthMechanisms = smtpAuthMechanisms;
        return this;
    }
    
    /**
     * @param checkInterval the checkInterval to set
     * @return 
     */
    public ConnectionSettingsIMAP setCheckInterval(int checkInterval) {
        this.checkInterval = checkInterval;
        return this;
    }
    
    /**
     * @param emailSendTimeout the emailSendTimeout to set
     * @return 
     */
    public ConnectionSettingsIMAP setEmailSendTimeout(int emailSendTimeout) {
        this.emailSendTimeout = emailSendTimeout;
        return this;
    }

    /**
     * @param maxMessageSize the maxMessageSize to set
     * @return 
     */
    public ConnectionSettingsIMAP setMaxMessageSize(int maxMessageSize) {
        this.maxMessageSize = maxMessageSize;
        return this;
    }

    /**
     * Check
     * @param object
     */
    private void checkNonNull(Object object) {
        if (object == null) {
            throw new IllegalArgumentException("Parameter must not be null");
        }
    }
    
    /**
     * Reads all IMAP connection settings from file but the password. The password will be provided with the passwordProvider parameter
     * 
     * @param file
     * @param passwordProvider
     * @return
     */
    public static ConnectionSettingsIMAP getConnectionIMAPSettingsFromFile(File file, PasswordProvider passwordProvider) {
        // Check
        if (file == null) { return null; }
        
        // Prepare
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream(file));
        } catch (IOException e) {          
            throw new IllegalStateException("Unable to load file to read ConnectionIMAPSettings", e);
        }
        
        // Check parameters
        if (prop.getProperty(EMAIL_ADDRESS_KEY) == null ||prop.getProperty(IMAP_SERVER_KEY) == null || prop.getProperty(IMAP_PORT_KEY) == null || prop.getProperty(IMAP_ENCRYPTION_TYPE) == null
            || prop.getProperty(SMTP_SERVER_KEY) == null || prop.getProperty(SMTP_PORT_KEY) == null || prop.getProperty(SMTP_ENCRYPTION_TYPE) == null) {            
            throw new IllegalStateException("Properties file does not contain all necessary fields!");
        }
        if (!(prop.getProperty(SMTP_ENCRYPTION_TYPE).equals(SSL_TLS) ||
              prop.getProperty(SMTP_ENCRYPTION_TYPE).equals(START_TLS)) ||
            !(prop.getProperty(IMAP_ENCRYPTION_TYPE).equals(SSL_TLS) ||
              prop.getProperty(IMAP_ENCRYPTION_TYPE).equals(SSL_TLS))) {
            throw new IllegalStateException(String.format("Please set eithr %s or %s for IMAP and SMTP encryption", SSL_TLS, START_TLS));
        }
        
        // Return
        return new ConnectionSettingsIMAP(prop.getProperty(EMAIL_ADDRESS_KEY),
                                          passwordProvider).setIMAPServer(prop.getProperty(IMAP_SERVER_KEY))
                                                           .setIMAPPort(Integer.valueOf(prop.getProperty(IMAP_PORT_KEY)))
                                                           .setSMTPServer(prop.getProperty(SMTP_SERVER_KEY))
                                                           .setSMTPPort(Integer.valueOf(prop.getProperty(SMTP_PORT_KEY)))
                                                           .setSSLTLSIMAP(Boolean.valueOf(prop.getProperty(IMAP_ENCRYPTION_TYPE).equals(SSL_TLS)))
                                                           .setSSLTLSSMTP(Boolean.valueOf(prop.getProperty(SMTP_ENCRYPTION_TYPE).equals(SSL_TLS)))
                                                           .setAcceptSelfSignedCertificates(prop.getProperty(ACCEPT_SELF_SIGNED_CERT_KEY) != null
                                                                   ? Boolean.valueOf(prop.getProperty(ACCEPT_SELF_SIGNED_CERT_KEY))
                                                                   : false)
                                                           .setSearchForProxy(prop.getProperty(USE_PROXY_KEY) != null
                                                                   ? Boolean.valueOf(prop.getProperty(USE_PROXY_KEY))
                                                                   : false);
    }
    
    @Override
    public String toString() {
        return String.format("IMAP connections for e-mail address %s", this.imapEmailAddress);
    }

    @Override
    public String getIdentifier() {
        return this.imapEmailAddress;
    }

    @Override
    public ExchangeMode getExchangeMode() {
        return ExchangeMode.EMAIL;
    }



    @Override
    public int hashCode() {
        return Objects.hash(acceptSelfSignedCert,
                            checkInterval,
                            emailSendTimeout,
                            imapAuthMechanisms,
                            imapEmailAddress,
                            imapPort,
                            imapServer,
                            imapUserName,
                            maxMessageSize,
                            provider,
                            searchForProxy,
                            smptEmailAddress,
                            smtpAuthMechanisms,
                            smtpPort,
                            smtpServer,
                            smtpUserName,
                            ssltlsIMAP,
                            ssltlsSMTP);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ConnectionSettingsIMAP other = (ConnectionSettingsIMAP) obj;
        return acceptSelfSignedCert == other.acceptSelfSignedCert &&
               checkInterval == other.checkInterval && emailSendTimeout == other.emailSendTimeout &&
               Objects.equals(imapAuthMechanisms, other.imapAuthMechanisms) &&
               Objects.equals(imapEmailAddress, other.imapEmailAddress) &&
               imapPort == other.imapPort && Objects.equals(imapServer, other.imapServer) &&
               Objects.equals(imapUserName, other.imapUserName) &&
               maxMessageSize == other.maxMessageSize && Objects.equals(provider, other.provider) &&
               searchForProxy == other.searchForProxy &&
               Objects.equals(smptEmailAddress, other.smptEmailAddress) &&
               Objects.equals(smtpAuthMechanisms, other.smtpAuthMechanisms) &&
               smtpPort == other.smtpPort && Objects.equals(smtpServer, other.smtpServer) &&
               Objects.equals(smtpUserName, other.smtpUserName) && ssltlsIMAP == other.ssltlsIMAP &&
               ssltlsSMTP == other.ssltlsSMTP;
    }
}