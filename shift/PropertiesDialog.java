package com.sshtools.shift;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import com.sshtools.common.ui.ResourceIcon;
import com.sshtools.common.ui.UIUtil;
import com.sshtools.j2ssh.SftpClient;
import com.sshtools.j2ssh.sftp.FileAttributes;

public class PropertiesDialog
    extends JDialog {

  private final static NumberFormat KB_FILE_SIZE_FORMAT = new DecimalFormat();
  private final static NumberFormat MB_FILE_SIZE_FORMAT = new DecimalFormat();

  JTabbedPane jTabbedPane1 = new JTabbedPane();
  JPanel mainpanel = new JPanel();
  JPanel headerPanel = new JPanel();
  JPanel navigate = new JPanel();
  BorderLayout b = new BorderLayout();

  JButton ok = new JButton("OK");
  JButton cancel = new JButton("Cancel");

  JCheckBox ownerRead = new JCheckBox("Read");
  JCheckBox ownerWrite = new JCheckBox("Write");
  JCheckBox ownerExec = new JCheckBox("Exec");
  JCheckBox groupRead = new JCheckBox("Read");
  JCheckBox groupWrite = new JCheckBox("Write");
  JCheckBox groupExec = new JCheckBox("Exec");
  JCheckBox otherRead = new JCheckBox("Read");
  JCheckBox otherWrite = new JCheckBox("Write");
  JCheckBox otherExec = new JCheckBox("Exec");

  JPanel paneldesc = new JPanel();
  JPanel panelone = new JPanel();
  JPanel paneltwo = new JPanel();
  JPanel panelthree = new JPanel();

  JTextField filename = new JTextField();
  JTextField size = new JTextField();
  JTextField location = new JTextField();
  JTextField kind = new JTextField();
  JTextField owner = new JTextField();
  JTextField group = new JTextField();
  JTextField modified = new JTextField();
  JLabel iconLabel = new JLabel();
  JPanel permissions = new JPanel();
  DirectoryListingTableModel model;
  FileAttributes attrib;
  String locationStr;
  String fileName;
  boolean editable;
  private SftpClient sftp;

  static {
    KB_FILE_SIZE_FORMAT.setMinimumFractionDigits(0);
    KB_FILE_SIZE_FORMAT.setMaximumFractionDigits(0);
  }

  static {
    MB_FILE_SIZE_FORMAT.setMinimumFractionDigits(0);
    MB_FILE_SIZE_FORMAT.setMaximumFractionDigits(2);
  }

  public PropertiesDialog(FileAttributes attrib,
                          SftpClient sftp,
                          String fileName,
                          DirectoryListingTableModel model) {

    try {
      this.attrib = attrib;
      this.fileName = fileName;
      this.sftp = sftp;
      this.locationStr = sftp.pwd();
      this.model = model;
      jbInit();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {

    b.setHgap(10);

    this.setModal(true);
    this.getContentPane().setLayout(b);
    this.getContentPane().add(jTabbedPane1, BorderLayout.CENTER);

    ResourceIcon fileIcon = new ResourceIcon(PropertiesDialog.class,
                                             "largefile.png");
    iconLabel.setIcon(fileIcon);

    headerPanel.setLayout(new BorderLayout());
    mainpanel.setLayout(new GridBagLayout());
    jTabbedPane1.addTab("Properties", mainpanel);
    Insets ins = new Insets(2, 30, 2, 10);
    Insets ins2 = new Insets(2, 7, 2, 30);

    filename.setBorder(BorderFactory.createLoweredBevelBorder());
    size.setBorder(BorderFactory.createLoweredBevelBorder());
    location.setBorder(BorderFactory.createLoweredBevelBorder());
    kind.setBorder(BorderFactory.createLoweredBevelBorder());
    owner.setBorder(BorderFactory.createLoweredBevelBorder());
    group.setBorder(BorderFactory.createLoweredBevelBorder());
    modified.setBorder(BorderFactory.createLoweredBevelBorder());

    paneldesc.setLayout(new GridLayout(3, 1));
    paneldesc.add(new JLabel("Owner"));
    paneldesc.add(new JLabel("Group"));
    paneldesc.add(new JLabel("Others"));

    panelone.setLayout(new GridLayout(3, 1));
    panelone.add(ownerRead);
    panelone.add(groupRead);
    panelone.add(otherRead);

    paneltwo.setLayout(new GridLayout(3, 1));
    paneltwo.add(ownerWrite);
    paneltwo.add(groupWrite);
    paneltwo.add(otherWrite);

    panelthree.setLayout(new GridLayout(3, 1));
    panelthree.add(ownerExec);
    panelthree.add(groupExec);
    panelthree.add(otherExec);

    permissions.setLayout(new GridLayout(1, 4));
    permissions.add(paneldesc);
    permissions.add(panelone);
    permissions.add(paneltwo);
    permissions.add(panelthree);

    filename.setFont(new Font("SansSerif", 1, 11));
    size.setFont(new Font("SansSerif", 0, 10));
    location.setFont(new Font("SansSerif", 0, 10));
    kind.setFont(new Font("SansSerif", 0, 10));
    owner.setFont(new Font("SansSerif", 0, 10));
    group.setFont(new Font("SansSerif", 0, 10));
    modified.setFont(new Font("SansSerif", 0, 10));

    GridBagConstraints gbc1 = new GridBagConstraints();

    gbc1.fill = GridBagConstraints.HORIZONTAL;
    gbc1.anchor = GridBagConstraints.NORTH;

    gbc1.weightx = 0.0;
    gbc1.insets = new Insets(2, 38, 12, 5);
    UIUtil.jGridBagAdd(mainpanel, iconLabel, gbc1,
                       GridBagConstraints.RELATIVE);
    gbc1.weightx = 1.0;
    gbc1.insets = new Insets(9, 7, 2, 30);
    UIUtil.jGridBagAdd(mainpanel, filename, gbc1,
                       GridBagConstraints.REMAINDER);

    gbc1.weightx = 0.0;
    gbc1.insets = ins;
    UIUtil.jGridBagAdd(mainpanel, new JLabel("Size"), gbc1,
                       GridBagConstraints.RELATIVE);
    gbc1.weightx = 1.0;
    gbc1.insets = ins2;
    UIUtil.jGridBagAdd(mainpanel, size, gbc1,
                       GridBagConstraints.REMAINDER);
    gbc1.weightx = 0.0;
    gbc1.insets = ins;
    UIUtil.jGridBagAdd(mainpanel, new JLabel("Where"), gbc1,
                       GridBagConstraints.RELATIVE);
    gbc1.weightx = 1.0;
    gbc1.insets = ins2;
    UIUtil.jGridBagAdd(mainpanel, location, gbc1,
                       GridBagConstraints.REMAINDER);
    gbc1.weightx = 0.0;
    gbc1.insets = ins;
    UIUtil.jGridBagAdd(mainpanel, new JLabel("Kind"), gbc1,
                       GridBagConstraints.RELATIVE);
    gbc1.weightx = 1.0;
    gbc1.insets = ins2;
    UIUtil.jGridBagAdd(mainpanel, kind, gbc1,
                       GridBagConstraints.REMAINDER);
    gbc1.weightx = 0.0;
    gbc1.insets = ins;
    UIUtil.jGridBagAdd(mainpanel, new JLabel("Owner"), gbc1,
                       GridBagConstraints.RELATIVE);
    gbc1.weightx = 1.0;
    gbc1.insets = ins2;
    UIUtil.jGridBagAdd(mainpanel, owner, gbc1,
                       GridBagConstraints.REMAINDER);
    gbc1.weightx = 0.0;
    gbc1.insets = ins;
    UIUtil.jGridBagAdd(mainpanel, new JLabel("Group"), gbc1,
                       GridBagConstraints.RELATIVE);
    gbc1.weightx = 1.0;
    gbc1.insets = ins2;
    UIUtil.jGridBagAdd(mainpanel, group, gbc1,
                       GridBagConstraints.REMAINDER);
    gbc1.weightx = 0.0;
    gbc1.insets = ins;
    UIUtil.jGridBagAdd(mainpanel, new JLabel("Modified"), gbc1,
                       GridBagConstraints.RELATIVE);
    gbc1.weightx = 1.0;
    gbc1.insets = ins2;
    UIUtil.jGridBagAdd(mainpanel, modified, gbc1,
                       GridBagConstraints.REMAINDER);

    gbc1.insets = new Insets(5, 5, 5, 5);
    UIUtil.jGridBagAdd(mainpanel, permissions, gbc1,
                       GridBagConstraints.REMAINDER);

    navigate.setBounds(new Rectangle(7, getHeight(), getWidth(), 29));
    //ok.setSize(100,25);

    FlowLayout flow = new FlowLayout();
    flow.setAlignment(FlowLayout.RIGHT);
    navigate.setLayout(flow);

    this.getContentPane().add(navigate, BorderLayout.SOUTH);
    navigate.add(ok);
    navigate.add(cancel);

    ok.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent evt) {
        applyProperties();
        dispose();
      }
    });

    cancel.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent evt) {
        dispose();
      }
    });

    setSize(280, 380);
    setResizable(false);
    populate();
    UIUtil.positionComponent(UIUtil.CENTER, this);
  }

  private void applyProperties() {

    int perm = attrib.getPermissions().intValue();
    int permcompare = perm;

    if (ownerRead.isSelected() &&
        ( (attrib.getPermissions().intValue() & FileAttributes.S_IRUSR) !=
         FileAttributes.S_IRUSR)) {
      perm = perm | FileAttributes.S_IRUSR;
    }
    else if (!ownerRead.isSelected() &&
             ( (attrib.getPermissions().intValue() & FileAttributes.S_IRUSR) ==
              FileAttributes.S_IRUSR)) {
      perm = perm ^ FileAttributes.S_IRUSR;
    }

    if (ownerWrite.isSelected() &&
        ( (attrib.getPermissions().intValue() & FileAttributes.S_IWUSR) !=
         FileAttributes.S_IWUSR)) {
      perm = perm | FileAttributes.S_IWUSR;
    }
    else if (!ownerWrite.isSelected() &&
             ( (attrib.getPermissions().intValue() & FileAttributes.S_IWUSR) ==
              FileAttributes.S_IWUSR)) {
      perm = perm ^ FileAttributes.S_IWUSR;
    }

    if (ownerExec.isSelected() &&
        ( (attrib.getPermissions().intValue() & FileAttributes.S_IXUSR) !=
         FileAttributes.S_IXUSR)) {
      perm = perm | FileAttributes.S_IXUSR;
    }
    else if (!ownerExec.isSelected() &&
             ( (attrib.getPermissions().intValue() & FileAttributes.S_IXUSR) ==
              FileAttributes.S_IXUSR)) {
      perm = perm ^ FileAttributes.S_IXUSR;
    }

    if (groupRead.isSelected() &&
        ( (attrib.getPermissions().intValue() & FileAttributes.S_IRGRP) !=
         FileAttributes.S_IRGRP)) {
      perm = perm | FileAttributes.S_IRGRP;
    }
    else if (!groupRead.isSelected() &&
             ( (attrib.getPermissions().intValue() & FileAttributes.S_IRGRP) ==
              FileAttributes.S_IRGRP)) {
      perm = perm ^ FileAttributes.S_IRGRP;
    }

    if (groupWrite.isSelected() &&
        ( (attrib.getPermissions().intValue() & FileAttributes.S_IWGRP) !=
         FileAttributes.S_IWGRP)) {
      perm = perm | FileAttributes.S_IWGRP;
    }
    else if (!groupWrite.isSelected() &&
             ( (attrib.getPermissions().intValue() & FileAttributes.S_IWGRP) ==
              FileAttributes.S_IWGRP)) {
      perm = perm ^ FileAttributes.S_IWGRP;
    }

    if (groupExec.isSelected() &&
        ( (attrib.getPermissions().intValue() & FileAttributes.S_IXGRP) !=
         FileAttributes.S_IXGRP)) {
      perm = perm | FileAttributes.S_IXGRP;
    }
    else if (!groupExec.isSelected() &&
             ( (attrib.getPermissions().intValue() & FileAttributes.S_IXGRP) ==
              FileAttributes.S_IXGRP)) {
      perm = perm ^ FileAttributes.S_IXGRP;
    }

    if (otherRead.isSelected() &&
        ( (attrib.getPermissions().intValue() & FileAttributes.S_IROTH) !=
         FileAttributes.S_IROTH)) {
      perm = perm | FileAttributes.S_IROTH;
    }
    else if (!otherRead.isSelected() &&
             ( (attrib.getPermissions().intValue() & FileAttributes.S_IROTH) ==
              FileAttributes.S_IROTH)) {
      perm = perm ^ FileAttributes.S_IROTH;
    }

    if (otherWrite.isSelected() &&
        ( (attrib.getPermissions().intValue() & FileAttributes.S_IWOTH) !=
         FileAttributes.S_IWOTH)) {
      perm = perm | FileAttributes.S_IWOTH;
    }
    else if (!otherWrite.isSelected() &&
             ( (attrib.getPermissions().intValue() & FileAttributes.S_IWOTH) ==
              FileAttributes.S_IWOTH)) {
      perm = perm ^ FileAttributes.S_IWOTH;
    }

    if (otherExec.isSelected() &&
        ( (attrib.getPermissions().intValue() & FileAttributes.S_IXOTH) !=
         FileAttributes.S_IXOTH)) {
      perm = perm | FileAttributes.S_IXOTH;
    }
    else if (!otherExec.isSelected() &&
             ( (attrib.getPermissions().intValue() & FileAttributes.S_IXOTH) ==
              FileAttributes.S_IXOTH)) {
      perm = perm ^ FileAttributes.S_IXOTH;
    }

    try {

      if (permcompare != perm) {
        sftp.chmod(perm, fileName);
      }

      if (!owner.getText().equals(String.valueOf(attrib.getUID().longValue())) &&
          !owner.getText().trim().equals("")) {

        // Change the owner if different
        Integer i = new Integer(owner.getText());
        sftp.chown(i.intValue(), fileName);
      }

      if (!group.getText().equals(String.valueOf(attrib.getGID().longValue())) &&
          !group.getText().trim().equals("")) {

        // Change the group if different
        Integer i = new Integer(group.getText());
        sftp.chgrp(i.intValue(), fileName);
      }

      if (!filename.getText().equals(fileName) &&
          !filename.getText().trim().equals("")) {

        // Rename the file/dir if different
        sftp.rename(fileName, filename.getText());
      }

      hide();
      model.refresh();
    }
    catch (IOException ex) {
      JOptionPane.showMessageDialog(this,
          "Error occurred whilst applying the properties");
    }
  }

  private void populate() {

    location.setText(locationStr);
    modified.setText(attrib.getModTimeString());
    owner.setText(String.valueOf(attrib.getUID().longValue()));
    group.setText(String.valueOf(attrib.getGID().longValue()));
    filename.setText(fileName);
    permissions.setBorder(BorderFactory.createTitledBorder("Permissions | " +
        attrib.getPermissionsString()));

    if ( (attrib.getPermissions().intValue() & FileAttributes.S_IRUSR) ==
        FileAttributes.S_IRUSR) {
      ownerRead.setSelected(true);
    }
    if ( (attrib.getPermissions().intValue() & FileAttributes.S_IRGRP) ==
        FileAttributes.S_IRGRP) {
      groupRead.setSelected(true);
    }
    if ( (attrib.getPermissions().intValue() & FileAttributes.S_IROTH) ==
        FileAttributes.S_IROTH) {
      otherRead.setSelected(true);
    }

    if ( (attrib.getPermissions().intValue() & FileAttributes.S_IWUSR) ==
        FileAttributes.S_IWUSR) {
      ownerWrite.setSelected(true);
    }
    if ( (attrib.getPermissions().intValue() & FileAttributes.S_IWGRP) ==
        FileAttributes.S_IWGRP) {
      groupWrite.setSelected(true);
    }
    if ( (attrib.getPermissions().intValue() & FileAttributes.S_IWOTH) ==
        FileAttributes.S_IWOTH) {
      otherWrite.setSelected(true);
    }

    if ( (attrib.getPermissions().intValue() & FileAttributes.S_IXUSR) ==
        FileAttributes.S_IXUSR) {
      ownerExec.setSelected(true);
    }
    if ( (attrib.getPermissions().intValue() & FileAttributes.S_IXGRP) ==
        FileAttributes.S_IXGRP) {
      groupExec.setSelected(true);
    }
    if ( (attrib.getPermissions().intValue() & FileAttributes.S_IXOTH) ==
        FileAttributes.S_IXOTH) {
      otherExec.setSelected(true);
    }

    if (attrib.isDirectory()) {
      kind.setText("Folder");
    }
    else {
      kind.setText("File");
    }

    double bytes = (double) attrib.getSize().longValue();

    if (bytes < 1024) {
      size.setText(String.valueOf( (int) bytes) + " bytes");
    }
    else if (bytes < 1048576) {
      size.setText(KB_FILE_SIZE_FORMAT.format(bytes / 1024) + " KB");
    }
    else {
      size.setText(MB_FILE_SIZE_FORMAT.format(bytes / 1024 / 1024) + " MB");
    }

    size.setEditable(false);
    location.setEditable(false);
    kind.setEditable(false);
    owner.setEditable(true);
    group.setEditable(true);
    modified.setEditable(false);
    filename.setEditable(true);
  }
}