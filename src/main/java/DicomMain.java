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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.lang.reflect.Field;
import java.util.List;
import java.util.*;


public class DicomMain extends JFrame {

    JProgressBar pg_bar;
    String[] colName = {"TagID", "Description"};
    Object[][] data1;
    Object[][] data2;
    File[] seriesPath;
    File studyPath;
    List<File> dcmPath;
    List<Map<String, String>> valueList;
    Map<String, String> map;


    public DicomMain() {
        //  JFrame
        setTitle("DicomTagExtraction");
        setSize(800, 500);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel jp = new JPanel();
        jp.setLayout(null);

        //  컴포넌트
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
        JScrollPane scroll_1 = new JScrollPane(tagTable_1);
        JScrollPane scroll_2 = new JScrollPane(tagTable_2);
        JTextField textField = new JTextField("File Path : ");
        JTextField searchBar = new JTextField();
        JButton choose_Btn = new JButton("폴더 선택");
        JButton extraction_Btn = new JButton("File Scan");
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
        choose_Btn.setBounds(520, 10, 120, 50);
        extraction_Btn.setBounds(655, 10, 120, 50);
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
        jp.add(excelExport_Btn);
        jp.add(save_Btn);
        jp.add(load_Btn);
        jp.add(add_Btn);
        jp.add(remove_Btn);
        add(jp);
        setVisible(true);

        //  이벤트
        // 폴더 선택
        choose_Btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dcmPath = new ArrayList<>();

                JFileChooser chooser = new JFileChooser();
                chooser.setCurrentDirectory(new File(System.getProperty("user.home") + "//" + "Desktop"));
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
                    tableModel_1.setRowCount(0);
                    tableModel_2.setRowCount(0);
                    textField.setText("파일 경로 : " + chooser.getSelectedFile().toString());
                    studyPath = chooser.getSelectedFile();
                    seriesPath = studyPath.listFiles();

                    for(int i =0 ; i<seriesPath.length;i++) {
                        File tmp = Arrays.stream(seriesPath[i].listFiles()).findFirst().get();
                        dcmPath.add(tmp);
                    }
                }
            }
        });

        //  스캔 버튼
        extraction_Btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if(tagTable_1.getRowCount() != 0 || dcmPath == null) {
                    return;
                }
                progress_start();

                List<String> tagList;
                List<String> decsList;
                valueList = new ArrayList<>();

                try {
                    for(int i = 0; i<dcmPath.size();i++){
                        DicomInputStream dis = new DicomInputStream(dcmPath.get(i));
                        Attributes m_attribs = dis.readDataset(-1, 0x7FE00010);

                        int[] tags = m_attribs.tags();
                        map = new HashMap<>();

                        for (int tag : tags) {
                            map.put(String.format("(%08X)", tag).substring(0, 5) + "," + String.format("(%08X)", tag).substring(5), m_attribs.getString(tag));
                        }
                        valueList.add(map);
                    }
                    tagList = new ArrayList<>();
                    decsList = new ArrayList<>();

                    Tag tag = new Tag();
                    Field[] nField = tag.getClass().getFields();
                    for(Field tmp : nField){
                        String desc = tmp.getName();
                        Object tags = tmp.get(desc);
                        tagList.add(String.format("(%08X)", tags).substring(0, 5) + "," + String.format("(%08X)", tags).substring(5));
                        decsList.add(desc);
                    }

                    data1 = new String[tagList.size()][2];
                    for (int i = 0; i < data1.length; i++) {
                        data1[i][0] = tagList.get(i);
                        data1[i][1] = decsList.get(i);
                        tableModel_1.addRow(data1[i]);
                    }
                } catch (IOException | IllegalAccessException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

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
        excelExport_Btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if(tagTable_2.getRowCount() == 0) {
                    return;
                }
                JFileChooser chooser = new JFileChooser();
                chooser.setCurrentDirectory(new File(System.getProperty("user.home") + "//" + "Desktop"));
                chooser.setFileFilter(new FileNameExtensionFilter(".xlsx", "xlsx"));
                if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                    File excelPath = new File(chooser.getSelectedFile().toString().replace(".xlsx",""));

                    try {
                        FileOutputStream fileOutput = new FileOutputStream(excelPath + ".xlsx");

                        XSSFWorkbook workbook = new XSSFWorkbook();
                        XSSFSheet sheet = workbook.createSheet();
                        XSSFRow Row;
                        XSSFCell cell;

                        Row = sheet.createRow(0);
                        for(int i = 0; i<tableModel_2.getRowCount(); i++) {
                            cell = Row.createCell(i + 1);
                            cell.setCellValue(tableModel_2.getValueAt(i, 0).toString());
                        }
                        Row = sheet.createRow(1);
                        for(int i = 0; i<tableModel_2.getRowCount(); i++) {
                            cell = Row.createCell(i + 1);
                            cell.setCellValue(tableModel_2.getValueAt(i, 1).toString());
                        }
                        for(int k = 0; k< seriesPath.length; k++) {
                            Row = sheet.createRow(k+2);
                            cell = Row.createCell(0);
                            cell.setCellValue(seriesPath[k].getName());
                            for (int i = 0; i < tableModel_2.getRowCount(); i++) {
                                cell = Row.createCell(i + 1);
                                cell.setCellValue(valueList.get(k).get(tableModel_2.getValueAt(i, 0)));
                            }
                        }
                        workbook.write(fileOutput);
                        fileOutput.close();
                        JOptionPane.showMessageDialog(null, "저장되었습니다.");
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
        });

        //  'SAVE' 버튼 - text export
        save_Btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (tagTable_2.getRowCount() == 0) {
                    return;
                }
                JFileChooser chooser = new JFileChooser();
                chooser.setCurrentDirectory(new File(System.getProperty("user.home") + "//" + "Desktop"));
                chooser.setFileFilter(new FileNameExtensionFilter(".txt", "txt"));
                if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                    File filePath = new File(chooser.getSelectedFile().toString().replace(".txt",""));

                    try {
                        BufferedWriter fw = new BufferedWriter(new FileWriter(filePath + ".txt"));
                        fw.write("[TagID]       [Description]" + "\n" + "-----------------------------------" + "\n");
                        for (int i = 0; i < tagTable_2.getRowCount(); i++) {
                            for (int j = 0; j < tagTable_2.getColumnCount(); j++) {
                                fw.write(tableModel_2.getValueAt(i, j) + " ");
                            }
                            fw.newLine();
                            fw.flush();
                        }
                        fw.close();
                        JOptionPane.showMessageDialog(null, "저장되었습니다.");
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
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
                    tableModel_2.setNumRows(0);

                    try {
                        BufferedReader fr = new BufferedReader(new FileReader(filePath));

                        Object[] lines = fr.lines().toArray();
                        for(int i=2; i<lines.length; i++){
                            String[] row = lines[i].toString().split(" ");
                            tableModel_2.addRow(row);
                        }
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
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




    public static void main(String[] args) {

        new DicomMain();
    }
}
