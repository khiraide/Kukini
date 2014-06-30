package gov.hawaii.digitalarchives.hida.persistentid;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.security.NoSuchAlgorithmException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springbyexample.util.log.AutowiredLogger;


public class NoidGUI extends javax.swing.JFrame 
{
    private static final long serialVersionUID = 1L;
    private String report, contact, directory, template, term, naan, naa, subnaa;
    private int noidNum;
    private boolean created;
    private JFileChooser chooser;
    private NoidGUIMethods noidInterface;
    private String ARK;
    @AutowiredLogger
	static Logger logger = LoggerFactory.getLogger(NoidGUI.class);

    public NoidGUI() throws NoSuchAlgorithmException, FileNotFoundException, IOException {
        initComponents();
        noidInterface = new NoidGUIMethods();

        createNoidButton.setEnabled(false);
    }

    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        outputArea = new javax.swing.JTextArea();
        titleLabel = new javax.swing.JLabel();
        directoryTextField = new javax.swing.JTextField();
        directoryLabel = new javax.swing.JLabel();
        templateLabel = new javax.swing.JLabel();
        templateTextField = new javax.swing.JTextField();
        naanLabel = new javax.swing.JLabel();
        naanTextField = new javax.swing.JTextField();
        submitButton = new javax.swing.JButton();
        outputLabel = new javax.swing.JLabel();
        autoButton = new javax.swing.JButton();
        warningLabel = new javax.swing.JLabel();
        createNoidButton = new javax.swing.JButton();
        getMsgButton = new javax.swing.JButton();
        ercButton = new javax.swing.JButton();
        numberOfNoids = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        outputArea.setEditable(false);
        outputArea.setColumns(20);
        outputArea.setRows(5);
        jScrollPane1.setViewportView(outputArea);

        titleLabel.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        titleLabel.setText("NOID Minter");

        directoryLabel.setText("Database Directory:");

        templateLabel.setText("Template:");

        templateTextField.setEditable(false);

        naanLabel.setText("NAAN:");

        naanTextField.setEditable(false);
        naanTextField.setText("70111");

        submitButton.setText("Submit");
        submitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                submitButtonActionPerformed(evt);
            }
        });

        outputLabel.setText("Output:");

        autoButton.setText("Auto");
        autoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoButtonActionPerformed(evt);
            }
        });

        warningLabel.setText("Submit \"Database Directory\" before proceeding.");

        createNoidButton.setText("Create NOID");
        createNoidButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createNoidButtonActionPerformed(evt);
            }
        });

        getMsgButton.setText("Get Message Log");
        getMsgButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getMsgButtonActionPerformed(evt);
            }
        });

        ercButton.setText("ERC");
        ercButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ercButtonActionPerformed(evt);
            }
        });

        numberOfNoids.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                numberOfNoidsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(templateLabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(templateTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(naanTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addComponent(warningLabel)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(directoryLabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(directoryTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(titleLabel))
                    .addComponent(naanLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(submitButton, javax.swing.GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE)
                            .addComponent(autoButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(createNoidButton, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(numberOfNoids)))))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(outputLabel)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 563, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(getMsgButton)
                        .addGap(18, 18, 18)
                        .addComponent(ercButton)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(titleLabel)
                        .addGap(17, 17, 17))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(getMsgButton)
                            .addComponent(ercButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(outputLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(directoryLabel)
                            .addComponent(directoryTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(warningLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(templateLabel)
                            .addComponent(templateTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(naanLabel)
                            .addComponent(naanTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(submitButton, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(autoButton, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(54, 54, 54)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(createNoidButton, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(numberOfNoids, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jScrollPane1)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void submitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submitButtonActionPerformed
 
        if (!created)
        {
            directory = directoryTextField.getText();
            File file = new File(directory);
            if(file.exists())
            {
                noidInterface.setNewNoid(directory);
                created = true;
                outputArea.setText("");
                outputArea.append("Directory: " + directory + " successfully created.\n");

                templateTextField.setEditable(true);
            }
            else
            {
                outputArea.append("Invalid directory provided.\n");
            }       
        }
        
        else if (created)
        {
            try
            {
                template = templateTextField.getText();
                naan = naanTextField.getText();

                report = noidInterface.getReport(template, naan);
                outputArea.append(report + "\n");

                for (int i = 0; i < noidNum; i++)
                {
                    ARK = noidInterface.mintNewId(true);
                    outputArea.append("ID = " + ARK + "\n");
                }
                createNoidButton.setEnabled(true);
                
            }catch(NumberFormatException e)
            {
                outputArea.append("# of NOIDs must be valid Integer.\n");
            }catch(NullPointerException e) {}
        }
        outputArea.setCaretPosition(0);
    }//GEN-LAST:event_submitButtonActionPerformed

    private void autoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoButtonActionPerformed
        
        outputArea.setText("");
        noidInterface.setNewNoid(System.getProperty("user.dir"));
        template = "f5.seedk";
        term = "long";
        naan = "70111";
        naa = "http://digitalarchives.hawaii.gov";
        subnaa = "dags";
        report = noidInterface.getReport(template, naan);
         
        outputArea.append(report + "\n\n");
        ARK = noidInterface.mintNewId(true);
        outputArea.append("ID = " + ARK + "\n");
        outputArea.setCaretPosition(0); 
        createNoidButton.setEnabled(true);
    }//GEN-LAST:event_autoButtonActionPerformed

    private void createNoidButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createNoidButtonActionPerformed
        if(numberOfNoids.getText().length() != 0)
        {
            try
            {
                noidNum = Integer.parseInt(numberOfNoids.getText());
                for (int i = 0; i < noidNum; i++) 
                {
                    ARK = noidInterface.mintNewId(true);
                    outputArea.append("ID = " + ARK + "\n");
                }
            }catch(NumberFormatException e)
            {
                outputArea.append("Please provide an integer.");
            }
        }
        else
        {
            ARK = noidInterface.mintNewId(true);
            outputArea.append("ID = " + ARK + "\n");
        }
    }//GEN-LAST:event_createNoidButtonActionPerformed

    private void getMsgButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getMsgButtonActionPerformed
        try
        {
            String log = noidInterface.getMsgLog();
            JOptionPane.showMessageDialog(null, log);
        }catch(NullPointerException e) {}
    }//GEN-LAST:event_getMsgButtonActionPerformed

    private void ercButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ercButtonActionPerformed
        try
        {
            String erc = noidInterface.getErc();
            JOptionPane.showMessageDialog(null, erc);
        }catch(NullPointerException e){}
    }//GEN-LAST:event_ercButtonActionPerformed

    private void numberOfNoidsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_numberOfNoidsActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_numberOfNoidsActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
        	NoidGUI.logger.error("Severe Exception in main: NoSuchAlgorithmException: {}", ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new NoidGUI().setVisible(true);
                } catch (NoSuchAlgorithmException ex) {
                	NoidGUI.logger.error("Severe Exception during run: NoSuchAlgorithmException: {}", ex);
                } catch (FileNotFoundException ex) {
                	NoidGUI.logger.error("Severe Exception during run: FileNotFoundException: {}", ex);
                } catch (IOException ex) {
                	NoidGUI.logger.error("Severe Exception during run: IOException: {}", ex);
                }
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton autoButton;
    private javax.swing.JButton createNoidButton;
    private javax.swing.JLabel directoryLabel;
    private javax.swing.JTextField directoryTextField;
    private javax.swing.JButton ercButton;
    private javax.swing.JButton getMsgButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel naanLabel;
    private javax.swing.JTextField naanTextField;
    private javax.swing.JTextField numberOfNoids;
    private javax.swing.JTextArea outputArea;
    private javax.swing.JLabel outputLabel;
    private javax.swing.JButton submitButton;
    private javax.swing.JLabel templateLabel;
    private javax.swing.JTextField templateTextField;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JLabel warningLabel;
    // End of variables declaration//GEN-END:variables
}
