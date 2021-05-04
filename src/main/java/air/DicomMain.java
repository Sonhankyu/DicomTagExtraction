package air;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;


public class DicomMain extends JFrame {



    final File configFile  = new File(System.getProperty("java.io.tmpdir")+File.separator+"dcmExConf");
    JProgressBar pg_bar;
    JFrame self;
    JTextArea cmd;
    JTextArea cmderror;
    String[] colName = {"TagID", "Description"};
    Object[][] data1;
    Object[][] data2;
    DefaultTableModel tableModel_1 = new DefaultTableModel(data1,colName){
        public boolean isCellEditable(int i, int c){
            return false;
        }
    };
    DefaultTableModel tableModel_2 = new DefaultTableModel(data2,colName){
        public boolean isCellEditable(int i, int c){
            return false;
        }
    };
    JTable tagTable_1 = new JTable(tableModel_1);
    JTable tagTable_2 = new JTable(tableModel_2);
    File[] seriesPath;
    File studyPath;
    List<File> dcmPath;
    List<Map<String, String>> valueList;
    Map<String, String> map;
    JFileChooser dirChoser;
    JFileChooser exChoser;
    int scanFileCnt = 0;
    JTextField textField = new JTextField("File Path : ");


    public void iniFileChooser(){
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        dirChoser = new JFileChooser() {
            @Override
            protected JDialog createDialog(Component parent) throws HeadlessException {
                // intercept the dialog created by JFileChooser
                JDialog dialog = super.createDialog(parent);

                dialog.setModal(true);  // set modality (or setModalityType)
                return dialog;
            }
        };
        dirChoser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        dirChoser.setCurrentDirectory(new File(System.getProperty("user.home") + "//" + "Desktop"));

        exChoser = new JFileChooser() {
            @Override
            protected JDialog createDialog(Component parent) throws HeadlessException {
                // intercept the dialog created by JFileChooser
                JDialog dialog = super.createDialog(parent);

                dialog.setModal(true);  // set modality (or setModalityType)
                return dialog;
            }
        };


        exChoser.setCurrentDirectory(new File(System.getProperty("user.home") + "//" + "Desktop"));
        exChoser.setFileFilter(new FileNameExtensionFilter(".xlsx", "xlsx"));

    }

    private void iniConfLoad(){
        if(configFile.exists())
            setCofTable(configFile);
    }

    private void iniSearchTable(){

        String [][] tmpList = Dictionary.getTagListAll();

        for(int i=0; i<tmpList.length;i++)
            tableModel_1.addRow(tmpList[i]);




    }


    public DicomMain() {
        //  JFrame
        self = this;
        setTitle("DicomTagExtraction");
        setSize(800, 800);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        iniFileChooser();

        JPanel jp = new JPanel();
        jp.setLayout(null);

        //  컴포넌트

        JScrollPane scroll_1 = new JScrollPane(tagTable_1);
        JScrollPane scroll_2 = new JScrollPane(tagTable_2);

        JTextField searchBar = new JTextField();
        JButton choose_Btn = new JButton("Selection");
        JButton extraction_Btn = new JButton("Header Extraction");
        JButton search_Btn = new JButton("검색");
        JButton excelExport_Btn = new JButton("Excel export");
        JButton save_Btn = new JButton("SAVE");
        JButton load_Btn = new JButton("LOAD");
        JButton add_Btn = new JButton(">>");
        JButton remove_Btn = new JButton("<<");
        TableRowSorter<TableModel> rowSorter = new TableRowSorter<>(tagTable_1.getModel());
        pg_bar = new JProgressBar();


        //  컴포넌트 설정
        textField.setBounds(10, 10, 490, 50);
        textField.setEnabled(false);
        tagTable_1.getColumnModel().getColumn(0).setPreferredWidth(10);
        tagTable_1.getColumnModel().getColumn(1).setPreferredWidth(150);
        tagTable_1.getTableHeader().setReorderingAllowed(false);
        tagTable_1.getTableHeader().setResizingAllowed(false);
        tagTable_1.setShowVerticalLines(false);
        tagTable_1.setShowHorizontalLines(false);
        tagTable_1.setRowSorter(rowSorter);
        scroll_1.setViewportView(tagTable_1);
        scroll_1.setBounds(10,170,340,280);
        scroll_1.getViewport().setBackground(Color.WHITE);
        tagTable_2.getColumnModel().getColumn(0).setPreferredWidth(10);
        tagTable_2.getColumnModel().getColumn(1).setPreferredWidth(150);
        tagTable_2.getTableHeader().setReorderingAllowed(false);
        tagTable_2.getTableHeader().setResizingAllowed(false);
        tagTable_2.setShowVerticalLines(false);
        tagTable_2.setShowHorizontalLines(false);
        scroll_2.setViewportView(tagTable_2);
        scroll_2.setBounds(435, 170, 340, 280);
        scroll_2.getViewport().setBackground(Color.WHITE);
        pg_bar.setBounds(10, 80, 765, 30);
        pg_bar.setValue(0);
        choose_Btn.setBounds(520, 10, 90, 50);
        extraction_Btn.setBounds(615, 10, 160, 50);

        searchBar.setBounds(10, 130, 270, 30);
        search_Btn.setBounds(290, 130, 60, 30);
        excelExport_Btn.setBounds(435,130,105,30);
        save_Btn.setBounds(605, 130, 80, 30);
        load_Btn.setBounds(695, 130, 80, 30);
        add_Btn.setBounds(365, 170, 55, 130);
        remove_Btn.setBounds(365, 320, 55, 130);

        jp.add(textField);
        jp.add(scroll_1);
        jp.add(scroll_2);
        jp.add(pg_bar);
        jp.add(searchBar);
        jp.add(choose_Btn);
        jp.add(extraction_Btn);
        jp.add(search_Btn);
       // jp.add(excelExport_Btn);
        jp.add(save_Btn);
        jp.add(load_Btn);
        jp.add(add_Btn);
        jp.add(remove_Btn);
        cmd = new JTextArea();
        cmd.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(cmd);
        cmd.setForeground(Color.BLUE);

        scrollPane.setBounds(10, 460, 765, 200);

        cmderror = new JTextArea();
        cmderror.setEditable(false);
        cmderror.setForeground(Color.RED);
        JScrollPane scrollPane2 = new JScrollPane(cmderror);


        scrollPane2.setBounds(10, 660, 765, 100);


        jp.add(scrollPane2);
        jp.add(scrollPane);
        add(jp);
        iniSearchTable();
        runFileDrop();
        iniConfLoad();
        setVisible(true);

        //  이벤트
        // 폴더 선택

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                saveConfFile(configFile);
                System.exit(0);
            }
        });

        choose_Btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dcmPath = new ArrayList<>();


                if(dirChoser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){





                    iniFile(dirChoser.getSelectedFile());


                }
            }
        });

        //  스캔 버튼
       /* extraction_Btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

              pg_bar.setMaximum(scanFileCnt);
                pg_bar.setStringPainted(true);


                final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {

                            try {
                                scanDir(studyPath);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                            try {
                                Thread.sleep(20);
                            } catch (InterruptedException ex) {}

                        return null;
                    }
                };
                worker.execute();


            }
        });*/

        //  '>>' 버튼
        add_Btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                int[] rows = tagTable_1.getSelectedRows();
                data2 = new Object[rows.length][2];

                for (int row : rows) {
                    int modelRow = tagTable_1.convertRowIndexToModel(row);
                    String tagId = tableModel_1.getValueAt(modelRow, 0).toString();
                    String desc = tableModel_1.getValueAt(modelRow, 1).toString();

                    if (!tableModel_2.getDataVector().toString().contains(tagId)) {
                        data2[0][0] = tagId;
                        data2[0][1] = desc;
                        tableModel_2.addRow(data2[0]);
                    }
                }
            }
        });

        //  '<<' 버튼
        remove_Btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if(tagTable_2.getRowCount() == 0 || tagTable_2.getSelectedRow() == -1){
                    return;
                }
                int[] selectRows = tagTable_2.getSelectedRows();
                for(int i = 0; i<selectRows.length; i++){
                    tableModel_2.removeRow(selectRows[i]-i);
                }
            }
        });

        // 엑셀 추출
        extraction_Btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if(tagTable_2.getRowCount() == 0) {
                    return;
                }


                List<String> exTagList = new ArrayList<>();
                for(int i = 0; i<tableModel_2.getRowCount(); i++) {
                    exTagList.add(tableModel_2.getValueAt(i, 1).toString());
                }

                List<Integer> intTagList = new ArrayList<>();
                for (String tmpTag : exTagList) {
                    intTagList.add(Dictionary.getTagByDes(tmpTag));
                }
                intTagList = intTagList.stream().sorted().collect(Collectors.toList());



                if (exChoser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                    File excelPath = new File(exChoser.getSelectedFile().toString().replace(".xlsx",""));

                    m_sacnFiles = new HashMap<>();




                    List<Integer> finalIntTagList = intTagList;
                    final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() throws Exception {

                            pg_bar.setMaximum(scanFileCnt);
                            pg_bar.setStringPainted(true);

                            try {
                                progressNum = 0;
                                resetdLog("Scan files...");
                                scanDir(studyPath);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                                appendLogError(e.toString());
                            }
                            try {
                                Thread.sleep(20);
                            } catch (InterruptedException ex) {}




                            pg_bar.setMaximum(m_sacnFiles.size());
                            pg_bar.setValue(0);
                            progressNum = 1;
                            pg_bar.setStringPainted(true);



                            try {
                                resetdLog("Export Excel files...");
                                FileOutputStream fileOutput = new FileOutputStream(excelPath + ".xlsx");

                                XSSFWorkbook workbook = new XSSFWorkbook();
                                XSSFSheet sheet = workbook.createSheet();
                                XSSFRow Row;
                                XSSFCell cell;

                                Row = sheet.createRow(0);

                                for(int i = 0; i<finalIntTagList.size(); i++) {
                                    cell = Row.createCell(i + 1);
                                    cell.setCellValue(Dictionary.getTag2PutteyString(finalIntTagList.get(i)));
                                }
                                Row = sheet.createRow(1);
                                cell = Row.createCell(0);
                                cell.setCellValue("File Ori Path");
                                for(int i = 0; i<tableModel_2.getRowCount(); i++) {
                                    cell = Row.createCell(i + 1);
                                    cell.setCellValue(Dictionary.getDescription(finalIntTagList.get(i)));
                                }

                                writeScanData4Excel(sheet, finalIntTagList);

                                workbook.write(fileOutput);
                                fileOutput.close();
                                resetdLog("Save Complete...");
                //                JOptionPane.showMessageDialog(null, "저장되었습니다.");
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                                appendLogError("Excel 파일을 사용중이거나, 파일을 쓸수 없습니다.");
                            }









                            try {
                                Thread.sleep(20);
                            } catch (InterruptedException ex) {}

                            return null;
                        }
                    };
                    worker.execute();


                }
            }
        });

        //  'SAVE' 버튼 - text export
        save_Btn.addActionListener(e -> {

            if (tagTable_2.getRowCount() == 0) {
                return;
            }
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File(System.getProperty("user.home") + "//" + "Desktop"));
            chooser.setFileFilter(new FileNameExtensionFilter(".txt", "txt"));
            if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                File filePath = new File(chooser.getSelectedFile().toString().replace(".txt",""));
                //filePath + ".txt"
                saveConfFile(new File(filePath.getAbsolutePath()+".txt"));
                JOptionPane.showMessageDialog(null, "저장되었습니다.");

            }
        });

        //  'LOAD' 버튼 - text import
        load_Btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                JFileChooser chooser = new JFileChooser();
                chooser.setCurrentDirectory(new File(System.getProperty("user.home") + "//" + "Desktop"));
                chooser.setFileFilter(new FileNameExtensionFilter(".txt", "txt"));
                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    File filePath = chooser.getSelectedFile();
                    setCofTable(filePath);



                }
            }
        });

        //  검색창
        searchBar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                String text = searchBar.getText();
                if(text.trim().length() == 0){
                    rowSorter.setRowFilter(null);
                }
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
        searchBar.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {}
            @Override
            public void keyReleased(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_ENTER) {
                    Toolkit.getDefaultToolkit().beep();
                    search_Btn.doClick();
                }
            }
        });

        // 검색 버튼
        search_Btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = searchBar.getText();

                if(text.trim().length() == 0){
                    rowSorter.setRowFilter(null);
                }else {
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });
    }

    public void progress_start() {
        try {
            pg_bar.setStringPainted(true);
            for (int i = 0; i <= 100; i++) {
                pg_bar.setValue(i);
                Thread.sleep(5);
                pg_bar.paintImmediately(0,0,800,30);
            }
            pg_bar.setValue(0);
            pg_bar.setStringPainted(false);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    int progressNum = 0;

    HashMap<String, DcmSortFile> m_sacnFiles = new HashMap<>();
    boolean chkClean = false;
    private void scanDir(File input) throws IOException {

        for (File file : input.listFiles()) {
            if (file.isDirectory()) {
                scanDir(file);
            } else if (file.isFile()) {
                setScanData(file);
                 progressNum++;
               // appendLog("("+progressNum+"/"+scanFileCnt+") File : "+ file.getAbsolutePath());

         //        System.out.println(progressNum);
                pg_bar.setValue(progressNum);
                if(chkClean)
                    return;
            }
        }
    }
    private void resetdLog(String txt){

        cmd.append(txt + "\n");  // 로그 내용을 JTextArea 위에 붙여주고
  //      cmd.setCaretPosition(cmd.getDocument().getLength());  // 맨아래로 스크롤한다.
    }

    private void appendLog(String txt){

        cmd.append(txt + "\n");  // 로그 내용을 JTextArea 위에 붙여주고
        cmd.setCaretPosition(cmd.getDocument().getLength());  // 맨아래로 스크롤한다.
    }

    private void appendLogError(String txt){

        cmderror.append(txt + "\n");  // 로그 내용을 JTextArea 위에 붙여주고
        cmderror.setCaretPosition(cmderror.getDocument().getLength());  // 맨아래로 스크롤한다.
    }



    private int countFileNumber(File input) throws IOException {
        int output =0;
        for (File file : input.listFiles()) {
            if (file.isDirectory()) {
                output += countFileNumber(file);
            } else if (file.isFile()) {
                output++;
            }
        }
        return output;
    }
    private void iniFile(File file){

        studyPath = file;
        pg_bar.setValue(0);
        textField.setText("파일 경로 : " + file.getAbsolutePath());
        try {
            scanFileCnt = countFileNumber(studyPath);
            pg_bar.setMaximum(scanFileCnt);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        seriesPath = studyPath.listFiles();
    }


    private void runFileDrop() {
        new SYBFileDrop(this, file -> new Thread(() ->
                iniFile(file[0])).start()
        );
    }

    private void writeScanData4Excel(XSSFSheet sheet, List<Integer> tagList){
        XSSFRow Row;
        XSSFCell cell;

        int idx = 2;






        for(DcmSortFile tmpDcm : m_sacnFiles.values()) {
            Row = sheet.createRow(idx++);
            cell = Row.createCell(0);
            cell.setCellValue(tmpDcm.file.getAbsolutePath());


            int colIdx = 1;
            for (int tmpTag : tagList) {
                cell = Row.createCell(colIdx++);
                String tmpData = tmpDcm.attribs.getString(tmpTag);
                if(tmpData==null)
                    tmpData = "NA";
                cell.setCellValue(tmpData);
            }
            pg_bar.setValue(progressNum++);
        }


    }

    private void setCofTable(File file){
        tableModel_2.setNumRows(0);

        try {
            BufferedReader fr = new BufferedReader(new FileReader(file));

            Object[] lines = fr.lines().toArray();
            for(int i=2; i<lines.length; i++){
                String[] row = lines[i].toString().split("\\|");
                tableModel_2.addRow(row);
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }







    public class DcmSortFile{
        public Attributes attribs;
        public File file;
        DcmSortFile(Attributes att , File file){
            this.attribs = att;
            this.file = file;
        }

    }

    private void setScanData(File input) throws IOException {

        try{
            DicomInputStream dis = new DicomInputStream(input);


        Attributes m_attribs = dis.readDataset(-1, 0x7FE00010);



        String key = m_attribs.getString(Tag.PatientID)+"_"+m_attribs.getString(Tag.StudyInstanceUID)+"_"+m_attribs.getString(Tag.SeriesInstanceUID);
        if(!m_sacnFiles.containsKey(key))
            m_sacnFiles.put(key, new DcmSortFile(m_attribs,input));
        }catch (Exception e){
            appendLogError("Can not read File :  " + input.getAbsolutePath());
        }
    }

    private void saveConfFile(File file){
        try {
            BufferedWriter fw = new BufferedWriter(new FileWriter(file));
            fw.write("[TagID]       [Description]" + "\n" + "-----------------------------------" + "\n");
            for (int i = 0; i < tagTable_2.getRowCount(); i++) {
                for (int j = 0; j < tagTable_2.getColumnCount(); j++) {
                    fw.write(tableModel_2.getValueAt(i, j) + "|");
                }
                fw.newLine();
                fw.flush();
            }
            fw.close();

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }







    public static void main(String[] args) {

        new DicomMain();
    }
}
