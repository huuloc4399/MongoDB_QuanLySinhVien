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
    private JComboBox<String> cbPhai;
    
    // Buttons
    private JButton btnAdd, btnUpdate, btnDelete, btnDeleteByClass;
    private JButton btnAddLanguage, btnAddSubject, btnUpdateSubject;
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
        btnAddLanguage = new JButton("[+] Thêm Ngoại ngữ");
        btnAddSubject = new JButton("[+] Thêm Môn học");
        btnUpdateSubject = new JButton("Sửa Điểm Môn");
        
        arrayBtnPanel.add(btnAddLanguage);
        arrayBtnPanel.add(btnAddSubject);
        arrayBtnPanel.add(btnUpdateSubject);
        
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
        txtSearch = new JTextField(20);
        btnSearch = new JButton("Tìm/Lọc theo Lớp/Mã");
        btnRefresh = new JButton("Làm mới bảng");
        searchPanel.add(new JLabel("Tìm kiếm (Mã SV hoặc Mã Lớp):"));
        searchPanel.add(txtSearch);
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
        btnAddSubject.addActionListener(e -> addSubject());
        btnUpdateSubject.addActionListener(e -> updateSubjectScore());
        
        btnSearch.addActionListener(e -> searchData());
        btnRefresh.addActionListener(e -> refreshTable());
    }

    // --- CÁC HÀM XỬ LÝ SỰ KIỆN ---

    public void refreshTable() {
        List<Document> docs = dao.getAllSinhVien();
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
            
            // Xử lý hiển thị mảng ngoại ngữ
            List<String> ngoaingu = doc.getList("ngoaingu", String.class);
            String nnStr = (ngoaingu != null) ? String.join(", ", ngoaingu) : "";
            
            // Xử lý hiển thị mảng môn học
            List<Document> monhoc = doc.getList("monhoc", Document.class);
            StringBuilder mhStr = new StringBuilder();
            if (monhoc != null) {
                for (Document mh : monhoc) {
                    mhStr.append(mh.getString("tenmon")).append("(").append(mh.get("diem")).append("); ");
                }
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
        String nn = JOptionPane.showInputDialog(this, "Nhập tên ngoại ngữ cần thêm cho sinh viên " + masv + ":");
        if (nn != null && !nn.trim().isEmpty()) {
            if (dao.addNgoaiNgu(masv, nn.trim())) {
                JOptionPane.showMessageDialog(this, "Đã thêm ngoại ngữ thành công.");
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, "Thêm thất bại. Kiểm tra lại mã SV.");
            }
        }
    }

    private void addSubject() {
        String masv = txtMaSV.getText().trim();
        if (masv.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sinh viên cần thêm môn học.");
            return;
        }
        
        // Custom Dialog for Subject
        JTextField mamonField = new JTextField(10);
        JTextField tenmonField = new JTextField(20);
        JTextField diemField = new JTextField(5);

        JPanel myPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        myPanel.add(new JLabel("Mã môn:"));
        myPanel.add(mamonField);
        myPanel.add(new JLabel("Tên môn:"));
        myPanel.add(tenmonField);
        myPanel.add(new JLabel("Điểm:"));
        myPanel.add(diemField);

        int result = JOptionPane.showConfirmDialog(null, myPanel, 
                 "Nhập thông tin môn học mới", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                double diem = Double.parseDouble(diemField.getText().trim());
                if (diem < 0.0 || diem > 10.0) {
                    JOptionPane.showMessageDialog(this, "Điểm phải từ 0.0 đến 10.0");
                    return;
                }
                Document monhoc = new Document("mamon", mamonField.getText().trim())
                        .append("tenmon", tenmonField.getText().trim())
                        .append("diem", diem);
                if (dao.addMonHoc(masv, monhoc)) {
                    JOptionPane.showMessageDialog(this, "Thêm môn học thành công.");
                    refreshTable();
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Điểm không hợp lệ.");
            }
        }
    }

    private void updateSubjectScore() {
        String masv = txtMaSV.getText().trim();
        if (masv.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sinh viên.");
            return;
        }
        String mamon = JOptionPane.showInputDialog(this, "Nhập MÃ MÔN cần sửa điểm:");
        if (mamon != null && !mamon.trim().isEmpty()) {
            String diemStr = JOptionPane.showInputDialog(this, "Nhập ĐIỂM MỚI (0-10):");
            if (diemStr != null && !diemStr.trim().isEmpty()) {
                try {
                    double diemMoi = Double.parseDouble(diemStr.trim());
                    if (diemMoi < 0.0 || diemMoi > 10.0) {
                        JOptionPane.showMessageDialog(this, "Điểm phải từ 0.0 đến 10.0");
                        return;
                    }
                    if (dao.updateDiemMonHoc(masv, mamon.trim(), diemMoi)) {
                        JOptionPane.showMessageDialog(this, "Sửa điểm thành công!");
                        refreshTable();
                    } else {
                        JOptionPane.showMessageDialog(this, "Không tìm thấy SV hoặc mã môn học này!");
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Điểm không hợp lệ.");
                }
            }
        }
    }

    private void searchData() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            refreshTable();
            return;
        }
        
        // Cố gắng tìm theo mã SV trước, nếu không có thì tìm theo Lớp
        Document sv = dao.getSinhVienByMasv(keyword);
        if (sv != null) {
            List<Document> result = new ArrayList<>();
            result.add(sv);
            loadDataToTable(result);
        } else {
            List<Document> list = dao.getSinhVienByLop(keyword);
            loadDataToTable(list);
        }
    }
}
