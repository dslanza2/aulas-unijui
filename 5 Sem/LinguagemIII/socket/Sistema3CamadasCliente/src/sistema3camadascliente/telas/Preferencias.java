/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * Preferencias.java
 *
 * Created on 21/03/2011, 20:38:03
 */

package sistema3camadascliente.telas;

import java.util.HashMap;
import javax.swing.JOptionPane;
import sistema3camadascliente.conexao.Cliente;

/**
 *
 * @author labinf
 */
public class Preferencias extends javax.swing.JDialog {

    /** Creates new form Preferencias */
    public Preferencias(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    public HashMap mostrar(){
        jTextField_Ip.setText(Cliente.getIp());
        jTextField_Porta.setText(String.valueOf(Cliente.getPorta()));

        super.setVisible(true);
        HashMap ob = new HashMap();
        ob.put("ip", jTextField_Ip.getText());
        ob.put("porta", jTextField_Porta.getText());
        return ob;
    }
    @Override
    @Deprecated
    public void setVisible(boolean b){
        JOptionPane.showMessageDialog(null, "Use o método mostrar()");
        dispose();
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextField_Ip = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jTextField_Porta = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jButton_Fechar = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.Y_AXIS));

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel1.setText("IP: ");
        jLabel1.setPreferredSize(new java.awt.Dimension(50, 18));
        jPanel1.add(jLabel1);

        jTextField_Ip.setPreferredSize(new java.awt.Dimension(150, 28));
        jPanel1.add(jTextField_Ip);

        getContentPane().add(jPanel1);

        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel2.setText("Porta: ");
        jLabel2.setPreferredSize(new java.awt.Dimension(50, 18));
        jPanel2.add(jLabel2);

        jTextField_Porta.setPreferredSize(new java.awt.Dimension(150, 28));
        jPanel2.add(jTextField_Porta);

        getContentPane().add(jPanel2);

        jPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jButton_Fechar.setText("Conectar");
        jButton_Fechar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_FecharActionPerformed(evt);
            }
        });
        jPanel4.add(jButton_Fechar);

        getContentPane().add(jPanel4);

        jPanel3.setLayout(new java.awt.BorderLayout());
        getContentPane().add(jPanel3);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-233)/2, (screenSize.height-146)/2, 233, 146);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton_FecharActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_FecharActionPerformed
        
        dispose();
    }//GEN-LAST:event_jButton_FecharActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                Preferencias dialog = new Preferencias(new javax.swing.JFrame(), true);
                HashMap mostrar = dialog.mostrar();
                System.out.println(mostrar.toString());
                System.exit(0);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton_Fechar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JTextField jTextField_Ip;
    private javax.swing.JTextField jTextField_Porta;
    // End of variables declaration//GEN-END:variables

}
