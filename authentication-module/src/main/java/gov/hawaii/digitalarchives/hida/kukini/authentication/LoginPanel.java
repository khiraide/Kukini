package gov.hawaii.digitalarchives.hida.kukini.authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a panel to allow users to enter their username and password
 * to authenticate.
 * 
 * @author Keone Hiraide
 */
public class LoginPanel extends javax.swing.JPanel {

    private static final Logger log = LoggerFactory.getLogger(LoginPanel.class);
    
    /**
     * Creates new form LoginPanel
     */
    public LoginPanel() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFrame1 = new javax.swing.JFrame();
        usernameField = new javax.swing.JTextField();
        passwordField = new javax.swing.JPasswordField();
        username = new javax.swing.JLabel();
        Password = new javax.swing.JLabel();
        titleLabel = new javax.swing.JLabel();
        subTitleLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();

        javax.swing.GroupLayout jFrame1Layout = new javax.swing.GroupLayout(jFrame1.getContentPane());
        jFrame1.getContentPane().setLayout(jFrame1Layout);
        jFrame1Layout.setHorizontalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jFrame1Layout.setVerticalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(new javax.swing.border.MatteBorder(null));
        setFocusable(false);
        setMaximumSize(new java.awt.Dimension(720, 576));
        setMinimumSize(new java.awt.Dimension(720, 576));
        setName(""); // NOI18N
        setPreferredSize(new java.awt.Dimension(720, 576));

        usernameField.setText(org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.usernameField.text")); // NOI18N
        usernameField.setToolTipText(org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.usernameField.toolTipText")); // NOI18N
        usernameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usernameFieldActionPerformed(evt);
            }
        });

        passwordField.setText(org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.passwordField.text")); // NOI18N
        passwordField.setToolTipText(org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.passwordField.toolTipText")); // NOI18N

        username.setText(org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.username.text")); // NOI18N

        Password.setText(org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.Password.text")); // NOI18N

        titleLabel.setFont(new java.awt.Font("DejaVu Serif", 1, 48)); // NOI18N
        titleLabel.setForeground(new java.awt.Color(0, 0, 102));
        titleLabel.setText(org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.titleLabel.text")); // NOI18N

        subTitleLabel.setFont(new java.awt.Font("WenQuanYi Micro Hei", 1, 14)); // NOI18N
        subTitleLabel.setText(org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.subTitleLabel.text")); // NOI18N

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gov/hawaii/digitalarchives/hida/kukini/authentication/Seal_of_the_State_of_Hawaii.jpeg"))); // NOI18N
        jLabel1.setText(org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.jLabel1.text")); // NOI18N

        jLabel2.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(0, 0, 0));
        jLabel2.setText(org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.jLabel2.text")); // NOI18N

        jLabel3.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(0, 0, 0));
        jLabel3.setText(org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.jLabel3.text")); // NOI18N

        jLabel4.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(0, 0, 0));
        jLabel4.setText(org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.jLabel4.text")); // NOI18N

        jLabel5.setForeground(new java.awt.Color(255, 0, 0));
        jLabel5.setText(org.openide.util.NbBundle.getMessage(LoginPanel.class, "LoginPanel.jLabel5.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(subTitleLabel)
                    .addComponent(titleLabel)
                    .addComponent(jLabel1)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(passwordField)
                        .addComponent(Password)
                        .addComponent(username)
                        .addComponent(usernameField, javax.swing.GroupLayout.PREFERRED_SIZE, 253, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(72, 72, 72)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2))
                .addContainerGap(70, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(111, 111, 111)
                .addComponent(jLabel4)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(titleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(subTitleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(217, 217, 217)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                .addComponent(username, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(usernameField)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Password, javax.swing.GroupLayout.DEFAULT_SIZE, 21, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(passwordField)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addGap(5, 5, 5))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void usernameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usernameFieldActionPerformed
        // TODO add your handling code here:
        log.debug("Entering usernameFieldActionPerformed(evt={})", evt);
        log.debug("Exiting usernameFieldActionPerformed(evt={})", evt);
    }//GEN-LAST:event_usernameFieldActionPerformed

    /**
     * @return The username that was entered within this login panel.
     */
    public String getUsername() {
        log.debug("Entering getUsername()");
        String username = usernameField.getText();
        log.debug("Exiting getUsername(): {}", username);
        return username;
    }
    
    /**
     * @return The password that was entered within this login panel.
     */
    public String getPasswordField() {
        log.debug("Entering getPasswordField()");
        String password = new String(passwordField.getPassword());
        log.debug("Exiting getPasswordField(): {}", password);
        return password;
    }
            
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel Password;
    private javax.swing.JFrame jFrame1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel subTitleLabel;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JLabel username;
    private javax.swing.JTextField usernameField;
    // End of variables declaration//GEN-END:variables
}
