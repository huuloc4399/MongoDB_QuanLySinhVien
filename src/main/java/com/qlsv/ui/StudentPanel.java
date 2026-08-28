package com.qlsv.ui;

import com.mongodb.MongoWriteException;
import com.qlsv.dao.SinhVienDAO;
import org.bson.Document;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class StudentPanel extends JPanel {
    private SinhVienDAO dao;
    private JTable table;
    private DefaultTableModel tableModel;

    // Form fields
    private JTextField txtMaSV, txtHoTen, txtTuoi, txtMaLop, txtSearch;
    private JComboBox<String> cbPhai, cbSort;
    
    // Buttons
    private JButton btnAdd, btnUpdate, btnDelete, btnDeleteByClass;
    private JButton btnAddLanguage, btnUpdateLanguage, btnDeleteLanguage;
    private JButton btnAddSubject, btnUpdateSubject, btnDeleteSubject;
    private JButton btnSearch, btnRefresh;

    public StudentPanel() {
        dao = new SinhVienDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initComponents();
        refreshTable();
    }

    private void initComponents() {
        // --- NỬA TRÊN: FORM VÀ CÁC NÚT TÍNH NĂNG ---
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        
        // 1. Form nhập liệu
        JPanel formPanel = new JPanel(new GridLayout(3, 4, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông tin Sinh viên"));

        formPanel.add(new JLabel("Mã SV:"));
        txtMaSV = new JTextField();
        formPanel.add(txtMaSV);

        formPanel.add(new JLabel("Họ tên:"));
        txtHoTen = new JTextField();
        formPanel.add(txtHoTen);

        formPanel.add(new JLabel("Tuổi:"));
        txtTuoi = new JTextField();
        formPanel.add(txtTuoi);

        formPanel.add(new JLabel("Giới tính:"));
        cbPhai = new JComboBox<>(new String[]{"Nam", "Nữ"});
        formPanel.add(cbPhai);

        formPanel.add(new JLabel("Mã lớp:"));
        txtMaLop = new JTextField();
        formPanel.add(txtMaLop);

        // Nút tính năng cơ bản
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnAdd = new JButton("Thêm SV");
        btnUpdate = new JButton("Sửa SV");
        btnDelete = new JButton("Xóa SV");
        btnDeleteByClass = new JButton("Xóa theo Lớp");
        
        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnDeleteByClass);
        
        // Nút tính năng mảng động
        JPanel arrayBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        arrayBtnPanel.setBorder(BorderFactory.createTitledBorder("Mảng Động & Nâng cao"));
        btnAddLanguage = new JButton("Thêm NN");
        btnUpdateLanguage = new JButton("Sửa NN");
        btnDeleteLanguage = new JButton("Xóa NN");
        btnAddSubject = new JButton("Thêm Môn");
        btnUpdateSubject = new JButton("Sửa Môn");
        btnDeleteSubject = new JButton("Xóa Môn");
        
        arrayBtnPanel.add(btnAddLanguage);
        arrayBtnPanel.add(btnUpdateLanguage);
        arrayBtnPanel.add(btnDeleteLanguage);
        arrayBtnPanel.add(btnAddSubject);
        arrayBtnPanel.add(btnUpdateSubject);
        arrayBtnPanel.add(btnDeleteSubject);
        
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.add(btnPanel, BorderLayout.NORTH);
        controlPanel.add(arrayBtnPanel, BorderLayout.CENTER);

        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(controlPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // --- NỬA DƯỚI: TÌM KIẾM VÀ BẢNG ---
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        
        // Thanh tìm kiếm
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtSearch = new JTextField(15);
        cbSort = new JComboBox<>(new String[]{"Mặc định", "Mã SV", "Họ tên", "Lớp"});
        btnSearch = new JButton("Tìm kiếm");
        btnRefresh = new JButton("Làm mới bảng");
        searchPanel.add(new JLabel("Tìm kiếm (Gần đúng):"));
        searchPanel.add(txtSearch);
        searchPanel.add(new JLabel("Sắp xếp theo:"));
        searchPanel.add(cbSort);
        searchPanel.add(btnSearch);
        searchPanel.add(btnRefresh);
        
        bottomPanel.add(searchPanel, BorderLayout.NORTH);

        // Bảng dữ liệu
        String[] columns = {"Mã SV", "Họ tên", "Tuổi", "Giới tính", "Lớp", "Ngoại ngữ", "Môn học (Điểm)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho phép sửa trực tiếp trên bảng
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.getSelectionModel().addListSelectionListener(e -> fillFormFromTable());
        
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    showStudentDetails();
                }
            }
        });
        
        // Căn chỉnh độ rộng cột
        table.getColumnModel().getColumn(0).setPreferredWidth(70);  // Mã SV
        table.getColumnModel().getColumn(1).setPreferredWidth(160); // Họ tên
        table.getColumnModel().getColumn(2).setPreferredWidth(50);  // Tuổi
        table.getColumnModel().getColumn(3).setPreferredWidth(70);  // Giới tính
        table.getColumnModel().getColumn(4).setPreferredWidth(80);  // Lớp
        table.getColumnModel().getColumn(5).setPreferredWidth(130); // Ngoại ngữ
        table.getColumnModel().getColumn(6).setPreferredWidth(350); // Môn học (Điểm)

        
        JScrollPane scrollPane = new JScrollPane(table);
        bottomPanel.add(scrollPane, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.CENTER);

        // --- GÁN SỰ KIỆN ---
        assignEvents();
    }

    private void assignEvents() {
        btnAdd.addActionListener(e -> addStudent());
        btnUpdate.addActionListener(e -> updateStudent());
        btnDelete.addActionListener(e -> deleteStudent());
        btnDeleteByClass.addActionListener(e -> deleteByClass());
        
        btnAddLanguage.addActionListener(e -> addLanguage());
        btnUpdateLanguage.addActionListener(e -> updateLanguage());
        btnDeleteLanguage.addActionListener(e -> deleteLanguage());
        btnAddSubject.addActionListener(e -> addSubject());
        btnUpdateSubject.addActionListener(e -> updateSubject());
        btnDeleteSubject.addActionListener(e -> deleteSubject());
        
        btnSearch.addActionListener(e -> searchData());
        btnRefresh.addActionListener(e -> refreshTable());
    }

    // --- CÁC HÀM XỬ LÝ SỰ KIỆN ---

    public void refreshTable() {
        if (txtSearch != null) txtSearch.setText("");
        if (cbSort != null) cbSort.setSelectedIndex(0);
        List<Document> docs = dao.searchSinhVien("", "Mặc định");
        loadDataToTable(docs);
    }

    private void loadDataToTable(List<Document> docs) {
        tableModel.setRowCount(0);
        for (Document doc : docs) {
            String masv = doc.getString("masv");
            String hoten = doc.getString("hoten");
            Integer tuoi = doc.getInteger("tuoi");
            String phai = doc.getString("phai");
            String malop = doc.getString("malop");
            
            // Xử lý hiển thị mảng ngoại ngữ (định dạng đẹp)
            List<String> ngoaingu = doc.getList("ngoaingu", String.class);
            String nnStr = "";
            if (ngoaingu != null && !ngoaingu.isEmpty()) {
                nnStr = "<html><font color='#E67E22'><b>" + String.join("</b>, <b>", ngoaingu) + "</b></font></html>";
            }
            
            // Xử lý hiển thị mảng môn học (định dạng HTML đẹp)
            List<Document> monhoc = doc.getList("monhoc", Document.class);
            StringBuilder mhStr = new StringBuilder();
            if (monhoc != null && !monhoc.isEmpty()) {
                mhStr.append("<html>");
                for (int i = 0; i < monhoc.size(); i++) {
                    Document mh = monhoc.get(i);
                    Object diemObj = mh.get("diem");
                    double diem = (diemObj instanceof Number) ? ((Number) diemObj).doubleValue() : 0.0;
                    
                    // Điểm dưới 5 thì màu đỏ, từ 5 trở lên thì màu xanh
                    String color = (diem >= 5.0) ? "#27AE60" : "#E74C3C";
                    
                    mhStr.append("<b>").append(mh.getString("tenmon")).append("</b>: ")
                         .append("<font color='").append(color).append("'>").append(diem).append("</font>");
                    
                    if (i < monhoc.size() - 1) {
                        mhStr.append(" &nbsp;|&nbsp; ");
                    }
                }
                mhStr.append("</html>");
            }
            
            tableModel.addRow(new Object[]{masv, hoten, tuoi, phai, malop, nnStr, mhStr.toString()});
        }
    }

    private void fillFormFromTable() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            txtMaSV.setText(tableModel.getValueAt(row, 0).toString());
            txtHoTen.setText(tableModel.getValueAt(row, 1).toString());
            txtTuoi.setText(tableModel.getValueAt(row, 2).toString());
            cbPhai.setSelectedItem(tableModel.getValueAt(row, 3).toString());
            txtMaLop.setText(tableModel.getValueAt(row, 4).toString());
        }
    }

    private void showStudentDetails() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            String masv = tableModel.getValueAt(row, 0).toString();
            Document sv = dao.getSinhVienByMasv(masv);
            if (sv != null) {
                StringBuilder details = new StringBuilder();
                details.append("Mã SV: ").append(sv.getString("masv")).append("\n");
                details.append("Họ tên: ").append(sv.getString("hoten")).append("\n");
                details.append("Tuổi: ").append(sv.getInteger("tuoi")).append("\n");
                details.append("Giới tính: ").append(sv.getString("phai")).append("\n");
                details.append("Lớp: ").append(sv.getString("malop")).append("\n\n");
                
                details.append("Ngoại ngữ:\n");
                List<String> ngoaingu = sv.getList("ngoaingu", String.class);
                if (ngoaingu != null && !ngoaingu.isEmpty()) {
                    for (String nn : ngoaingu) {
                        details.append("- ").append(nn).append("\n");
                    }
                } else {
                    details.append("(Không có)\n");
                }
                
                details.append("\nMôn học:\n");
                List<Document> monhoc = sv.getList("monhoc", Document.class);
                if (monhoc != null && !monhoc.isEmpty()) {
                    for (Document mh : monhoc) {
                        details.append("- ").append(mh.getString("tenmon"))
                               .append(" (").append(mh.getString("mamon")).append("): ")
                               .append(mh.get("diem")).append(" điểm\n");
                    }
                } else {
                    details.append("(Không có)\n");
                }
                
                JTextArea textArea = new JTextArea(details.toString());
                textArea.setEditable(false);
                textArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(400, 300));
                
                JOptionPane.showMessageDialog(this, scrollPane, "Chi tiết sinh viên", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private boolean validateForm() {
        if (txtMaSV.getText().trim().isEmpty() || txtHoTen.getText().trim().isEmpty() || 
            txtTuoi.getText().trim().isEmpty() || txtMaLop.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
            return false;
        }
        try {
            int tuoi = Integer.parseInt(txtTuoi.getText().trim());
            if (tuoi <= 0) {
                JOptionPane.showMessageDialog(this, "Tuổi phải là số nguyên dương hợp lệ (> 0).");
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Tuổi phải là số!");
            return false;
        }
        return true;
    }

    private void addStudent() {
        if (!validateForm()) return;
        
        try {
            Document doc = new Document("masv", txtMaSV.getText().trim())
                    .append("hoten", txtHoTen.getText().trim())
                    .append("tuoi", Integer.parseInt(txtTuoi.getText().trim()))
                    .append("phai", cbPhai.getSelectedItem().toString())
                    .append("malop", txtMaLop.getText().trim())
                    .append("ngoaingu", new ArrayList<String>())
                    .append("monhoc", new ArrayList<Document>());
            
            dao.addSinhVien(doc);
            JOptionPane.showMessageDialog(this, "Thêm sinh viên thành công!");
            refreshTable();
        } catch (MongoWriteException e) {
            if (e.getCode() == 11000) { // Duplicate key error code
                JOptionPane.showMessageDialog(this, "Lỗi: Mã sinh viên đã tồn tại (Vi phạm Unique Index).");
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi thêm: " + e.getMessage());
            }
        }
    }

    private void updateStudent() {
        if (!validateForm()) return;
        
        String masv = txtMaSV.getText().trim();
        boolean success = dao.updateThongTinCoBan(
            masv, 
            txtHoTen.getText().trim(), 
            Integer.parseInt(txtTuoi.getText().trim()), 
            cbPhai.getSelectedItem().toString(), 
            txtMaLop.getText().trim()
        );
        
        if (success) {
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            refreshTable();
        } else {
            JOptionPane.showMessageDialog(this, "Không tìm thấy sinh viên có mã: " + masv);
        }
    }

    private void deleteStudent() {
        String masv = txtMaSV.getText().trim();
        if (masv.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập/chọn mã SV cần xóa.");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa sinh viên " + masv + "?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (dao.deleteSinhVien(masv)) {
                JOptionPane.showMessageDialog(this, "Xóa thành công.");
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy sinh viên.");
            }
        }
    }

    private void deleteByClass() {
        String malop = txtMaLop.getText().trim();
        if (malop.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập mã Lớp cần xóa.");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, "CẢNH BÁO: Xóa tất cả sinh viên thuộc lớp " + malop + "?", "Xác nhận xóa diện rộng", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            long count = dao.deleteSinhVienByLop(malop);
            JOptionPane.showMessageDialog(this, "Đã xóa " + count + " sinh viên thuộc lớp " + malop);
            refreshTable();
        }
    }

    private void addLanguage() {
        String masv = txtMaSV.getText().trim();
        if (masv.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sinh viên cần thêm ngoại ngữ.");
            return;
        }

        JComboBox<String> cbNgoaiNgu = new JComboBox<>();
        cbNgoaiNgu.addItem("(Thêm ngoại ngữ mới...)");

        List<Document> dsNgoaiNgu = dao.getThongKeNgoaiNgu(""); // Lấy tất cả ngoại ngữ
        for (Document doc : dsNgoaiNgu) {
            cbNgoaiNgu.addItem(doc.getString("_id"));
        }

        JTextField txtNewNn = new JTextField(15);
        
        cbNgoaiNgu.addActionListener(e -> {
            int idx = cbNgoaiNgu.getSelectedIndex();
            if (idx == 0) {
                txtNewNn.setText("");
                txtNewNn.setEditable(true);
            } else {
                txtNewNn.setText(cbNgoaiNgu.getSelectedItem().toString());
                txtNewNn.setEditable(false);
            }
        });

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Ngoại ngữ có sẵn:"));
        panel.add(cbNgoaiNgu);
        panel.add(new JLabel("Tên ngoại ngữ:"));
        panel.add(txtNewNn);

        int result = JOptionPane.showConfirmDialog(this, panel, "Thêm Ngoại Ngữ", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String nn = txtNewNn.getText().trim();
            if (nn.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập/chọn tên ngoại ngữ.");
                return;
            }
            Document sv = dao.getSinhVienByMasv(masv);
            if (sv == null) {
                JOptionPane.showMessageDialog(this, "Sinh viên không tồn tại. Kiểm tra lại mã SV.");
                return;
            }
            
            List<String> currentLangs = sv.getList("ngoaingu", String.class);
            if (currentLangs != null && currentLangs.contains(nn)) {
                JOptionPane.showMessageDialog(this, "Thêm thất bại. Ngôn ngữ '" + nn + "' đã tồn tại cho sinh viên này!");
                return;
            }
            
            if (dao.addNgoaiNgu(masv, nn)) {
                JOptionPane.showMessageDialog(this, "Đã thêm ngoại ngữ thành công.");
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, "Thêm thất bại vì lỗi không xác định.");
            }
        }
    }

    private void addSubject() {
        String masv = txtMaSV.getText().trim();
        if (masv.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sinh viên cần thêm môn học.");
            return;
        }
        
        Document sv = dao.getSinhVienByMasv(masv);
        List<Document> monhocDaHoc = sv != null ? sv.getList("monhoc", Document.class) : new ArrayList<>();
        List<String> maMonDaHoc = new ArrayList<>();
        if (monhocDaHoc != null) {
            for (Document mh : monhocDaHoc) {
                maMonDaHoc.add(mh.getString("mamon"));
            }
        }
        
        JComboBox<String> cbMonHoc = new JComboBox<>();
        cbMonHoc.addItem("(Thêm môn mới...)");
        
        List<Document> dsMon = dao.getThongKeMaMonVaTenMon("");
        for (Document mon : dsMon) {
            String mamon = mon.getString("mamon");
            if (!maMonDaHoc.contains(mamon)) {
                cbMonHoc.addItem(mamon + " - " + mon.getString("tenmon"));
            }
        }

        JTextField mamonField = new JTextField(10);
        JTextField tenmonField = new JTextField(20);
        JTextField diemField = new JTextField(5);
        
        cbMonHoc.addActionListener(e -> {
            int idx = cbMonHoc.getSelectedIndex();
            if (idx == 0) {
                mamonField.setText("");
                tenmonField.setText("");
                mamonField.setEditable(true);
                tenmonField.setEditable(true);
            } else {
                String selected = cbMonHoc.getSelectedItem().toString();
                String[] parts = selected.split(" - ", 2);
                if (parts.length == 2) {
                    mamonField.setText(parts[0]);
                    tenmonField.setText(parts[1]);
                    mamonField.setEditable(false);
                    tenmonField.setEditable(false);
                }
            }
        });

        JPanel myPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        myPanel.add(new JLabel("Môn học có sẵn:"));
        myPanel.add(cbMonHoc);
        myPanel.add(new JLabel("Mã môn:"));
        myPanel.add(mamonField);
        myPanel.add(new JLabel("Tên môn:"));
        myPanel.add(tenmonField);
        myPanel.add(new JLabel("Điểm:"));
        myPanel.add(diemField);

        int result = JOptionPane.showConfirmDialog(null, myPanel, 
                 "Nhập thông tin môn học mới", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String mamonInput = mamonField.getText().trim();
            if (mamonInput.isEmpty() || tenmonField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập/chọn mã môn và tên môn.");
                return;
            }
            if (maMonDaHoc.contains(mamonInput)) {
                JOptionPane.showMessageDialog(this, "Sinh viên đã học môn này rồi, không thể thêm nữa.");
                return;
            }
            try {
                double diem = Double.parseDouble(diemField.getText().trim());
                if (diem < 0.0 || diem > 10.0) {
                    JOptionPane.showMessageDialog(this, "Điểm phải từ 0.0 đến 10.0");
                    return;
                }
                Document monhoc = new Document("mamon", mamonInput)
                        .append("tenmon", tenmonField.getText().trim())
                        .append("diem", diem);
                if (dao.addMonHoc(masv, monhoc)) {
                    JOptionPane.showMessageDialog(this, "Thêm môn học thành công.");
                    searchData();
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Điểm không hợp lệ.");
            }
        }
    }

    private void updateLanguage() {
        String masv = txtMaSV.getText().trim();
        if (masv.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sinh viên.");
            return;
        }
        Document sv = dao.getSinhVienByMasv(masv);
        if (sv == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy sinh viên.");
            return;
        }
        List<String> ngoaingu = sv.getList("ngoaingu", String.class);
        if (ngoaingu == null || ngoaingu.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Sinh viên này chưa có ngoại ngữ nào.");
            return;
        }
        JComboBox<String> cbNgoaiNgu = new JComboBox<>(ngoaingu.toArray(new String[0]));
        JTextField txtNewNn = new JTextField(15);
        
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Chọn ngoại ngữ cần sửa:"));
        panel.add(cbNgoaiNgu);
        panel.add(new JLabel("Tên ngoại ngữ mới:"));
        panel.add(txtNewNn);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Sửa Ngoại Ngữ", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String oldNn = (String) cbNgoaiNgu.getSelectedItem();
            String newNn = txtNewNn.getText().trim();
            if (newNn.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập tên ngoại ngữ mới.");
                return;
            }
            if (dao.updateNgoaiNgu(masv, oldNn, newNn)) {
                JOptionPane.showMessageDialog(this, "Sửa ngoại ngữ thành công!");
                searchData();
            } else {
                JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi sửa.");
            }
        }
    }

    private void deleteLanguage() {
        String masv = txtMaSV.getText().trim();
        if (masv.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sinh viên.");
            return;
        }
        Document sv = dao.getSinhVienByMasv(masv);
        if (sv == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy sinh viên.");
            return;
        }
        List<String> ngoaingu = sv.getList("ngoaingu", String.class);
        if (ngoaingu == null || ngoaingu.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Sinh viên này chưa có ngoại ngữ nào.");
            return;
        }
        JComboBox<String> cbNgoaiNgu = new JComboBox<>(ngoaingu.toArray(new String[0]));
        
        JPanel panel = new JPanel(new GridLayout(1, 2, 5, 5));
        panel.add(new JLabel("Chọn ngoại ngữ cần xóa:"));
        panel.add(cbNgoaiNgu);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Xóa Ngoại Ngữ", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String nn = (String) cbNgoaiNgu.getSelectedItem();
            int confirm = JOptionPane.showConfirmDialog(this, "Xóa ngoại ngữ '" + nn + "'?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (dao.deleteNgoaiNgu(masv, nn)) {
                    JOptionPane.showMessageDialog(this, "Xóa ngoại ngữ thành công!");
                    searchData();
                } else {
                    JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi xóa.");
                }
            }
        }
    }

    private void updateSubject() {
        String masv = txtMaSV.getText().trim();
        if (masv.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sinh viên.");
            return;
        }
        Document sv = dao.getSinhVienByMasv(masv);
        if (sv == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy sinh viên.");
            return;
        }
        List<Document> monhoc = sv.getList("monhoc", Document.class);
        if (monhoc == null || monhoc.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Sinh viên này chưa có môn học nào.");
            return;
        }
        
        JComboBox<String> cbMonHoc = new JComboBox<>();
        for (Document mh : monhoc) {
            cbMonHoc.addItem(mh.getString("mamon") + " - " + mh.getString("tenmon"));
        }
        
        JTextField tenmonField = new JTextField(20);
        JTextField diemField = new JTextField(5);
        
        cbMonHoc.addActionListener(e -> {
            int idx = cbMonHoc.getSelectedIndex();
            if (idx >= 0 && idx < monhoc.size()) {
                Document mh = monhoc.get(idx);
                tenmonField.setText(mh.getString("tenmon"));
                Object diemObj = mh.get("diem");
                diemField.setText(diemObj != null ? diemObj.toString() : "");
            }
        });
        
        if (cbMonHoc.getItemCount() > 0) {
            cbMonHoc.setSelectedIndex(0);
        }

        JPanel myPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        myPanel.add(new JLabel("Chọn môn (cần sửa):"));
        myPanel.add(cbMonHoc);
        myPanel.add(new JLabel("Tên môn mới:"));
        myPanel.add(tenmonField);
        myPanel.add(new JLabel("Điểm mới:"));
        myPanel.add(diemField);

        int result = JOptionPane.showConfirmDialog(null, myPanel, 
                 "Sửa thông tin môn học", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            int idx = cbMonHoc.getSelectedIndex();
            if (idx < 0) return;
            String mamon = monhoc.get(idx).getString("mamon");
            String tenmon = tenmonField.getText().trim();
            if (tenmon.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập tên môn.");
                return;
            }
            try {
                double diem = Double.parseDouble(diemField.getText().trim());
                if (diem < 0.0 || diem > 10.0) {
                    JOptionPane.showMessageDialog(this, "Điểm phải từ 0.0 đến 10.0");
                    return;
                }
                if (dao.updateMonHoc(masv, mamon, tenmon, diem)) {
                    JOptionPane.showMessageDialog(this, "Sửa môn học thành công!");
                    searchData();
                } else {
                    JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi sửa!");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Điểm không hợp lệ.");
            }
        }
    }

    private void deleteSubject() {
        String masv = txtMaSV.getText().trim();
        if (masv.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sinh viên.");
            return;
        }
        Document sv = dao.getSinhVienByMasv(masv);
        if (sv == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy sinh viên.");
            return;
        }
        List<Document> monhoc = sv.getList("monhoc", Document.class);
        if (monhoc == null || monhoc.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Sinh viên này chưa có môn học nào.");
            return;
        }
        JComboBox<String> cbMonHoc = new JComboBox<>();
        for (Document mh : monhoc) {
            cbMonHoc.addItem(mh.getString("mamon") + " - " + mh.getString("tenmon"));
        }
        
        JPanel panel = new JPanel(new GridLayout(1, 2, 5, 5));
        panel.add(new JLabel("Chọn môn cần xóa:"));
        panel.add(cbMonHoc);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Xóa Môn Học", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            int idx = cbMonHoc.getSelectedIndex();
            if (idx < 0) return;
            String mamon = monhoc.get(idx).getString("mamon");
            String displayStr = (String) cbMonHoc.getSelectedItem();
            
            int confirm = JOptionPane.showConfirmDialog(this, "Xóa môn học '" + displayStr + "'?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (dao.deleteMonHoc(masv, mamon)) {
                    JOptionPane.showMessageDialog(this, "Xóa môn học thành công!");
                    searchData();
                } else {
                    JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi xóa!");
                }
            }
        }
    }

    private void searchData() {
        String keyword = txtSearch.getText().trim();
        String sortType = cbSort != null ? cbSort.getSelectedItem().toString() : "Mặc định";
        List<Document> list = dao.searchSinhVien(keyword, sortType);
        loadDataToTable(list);
    }
}
