package com.qlsv.ui;

import com.qlsv.dao.SinhVienDAO;
import org.bson.Document;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.style.Styler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DashboardPanel extends JPanel {
    private SinhVienDAO dao;

    // Filter
    private JComboBox<String> cbClassFilter;

    // KPI Labels
    private JLabel lblTotalStudents, lblTotalClasses, lblAvgScore, lblGenderRatio;

    // Tables
    private JTable tblClassStats, tblTopStudents, tblHocLuc;

    // Charts
    private PieChart pieChartGender;
    private CategoryChart barChartLang;
    private XChartPanel<PieChart> panelPieChart;
    private XChartPanel<CategoryChart> panelBarChart;

    public DashboardPanel() {
        dao = new SinhVienDAO();
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        initComponents();
    }

    private void initComponents() {
        // --- 0. THANH CÔNG CỤ FILTER ---
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Lọc theo lớp: "));
        cbClassFilter = new JComboBox<>();
        filterPanel.add(cbClassFilter);
        cbClassFilter.addActionListener(e -> {
            if (cbClassFilter.getSelectedItem() != null) {
                refreshData();
            }
        });

        // --- 1. THẺ CHỈ SỐ TỔNG QUAN (KPI Cards) ---
        JPanel kpiPanel = new JPanel(new GridLayout(1, 4, 15, 0));

        lblTotalStudents = createKpiCard("Tổng SV", "0");
        lblTotalClasses = createKpiCard("Tổng Lớp", "0");
        lblAvgScore = createKpiCard("Điểm TB", "0.0");
        lblGenderRatio = createKpiCard("Tỉ lệ Nam/Nữ", "0/0");

        kpiPanel.add(lblTotalStudents.getParent());
        kpiPanel.add(lblTotalClasses.getParent());
        kpiPanel.add(lblAvgScore.getParent());
        kpiPanel.add(lblGenderRatio.getParent());

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(filterPanel, BorderLayout.NORTH);
        topContainer.add(kpiPanel, BorderLayout.CENTER);

        add(topContainer, BorderLayout.NORTH);

        // --- 2. CÁC BẢNG & BIỂU ĐỒ ---
        JPanel centerPanel = new JPanel(new GridLayout(3, 2, 15, 15));

        // Bảng 1: Top 5 Sinh viên
        tblTopStudents = new JTable(new DefaultTableModel(new Object[] { "Mã SV", "Họ tên", "Lớp", "Điểm TB" }, 0));
        centerPanel.add(createTablePanel("Top 5 Sinh viên điểm cao nhất", tblTopStudents));

        // Biểu đồ 1: Tỉ lệ Nam/Nữ
        pieChartGender = new PieChartBuilder().width(400).height(300).title("Tỉ lệ Nam/Nữ").build();
        pieChartGender.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        panelPieChart = new XChartPanel<>(pieChartGender);
        centerPanel.add(panelPieChart);

        // Bảng 2: Thống kê theo lớp (Luôn hiện toàn bộ lớp)
        tblClassStats = new JTable(
                new DefaultTableModel(new Object[] { "Mã Lớp", "Sĩ số", "ĐTB Cao nhất", "ĐTB Thấp nhất" }, 0));
        centerPanel.add(createTablePanel("Thống kê theo Lớp", tblClassStats));

        // Biểu đồ 2: Ngoại ngữ phổ biến
        barChartLang = new CategoryChartBuilder().width(400).height(300).title("Ngoại ngữ phổ biến")
                .xAxisTitle("Ngoại ngữ").yAxisTitle("Số lượng").build();
        barChartLang.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        // barChartLang.getStyler().setHasAnnotations(true); // Hiển thị số ở giữa cột
        panelBarChart = new XChartPanel<>(barChartLang);
        centerPanel.add(panelBarChart);

        // Bảng 3: Phân loại học lực
        tblHocLuc = new JTable(new DefaultTableModel(new Object[] { "Học lực", "Số lượng" }, 0));
        centerPanel.add(createTablePanel("Phân loại học lực", tblHocLuc));

        // Panel trống để lấp đầy GridLayout
        JPanel emptyPanel = new JPanel();
        emptyPanel.setOpaque(false);
        centerPanel.add(emptyPanel);

        add(centerPanel, BorderLayout.CENTER);
    }

    private JLabel createKpiCard(String title, String initialValue) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        panel.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(Color.GRAY);

        JLabel lblValue = new JLabel(initialValue, SwingConstants.CENTER);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblValue.setForeground(new Color(0, 102, 204));

        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(lblValue, BorderLayout.CENTER);

        return lblValue;
    }

    private JPanel createTablePanel(String title, JTable table) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        table.setRowHeight(22);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    public void updateFilterOptions() {
        String current = (String) cbClassFilter.getSelectedItem();

        // Remove action listener temporarily to prevent multiple refreshData calls
        java.awt.event.ActionListener[] listeners = cbClassFilter.getActionListeners();
        for (java.awt.event.ActionListener l : listeners) {
            cbClassFilter.removeActionListener(l);
        }

        cbClassFilter.removeAllItems();
        cbClassFilter.addItem("Tất cả");
        List<String> classes = dao.getAllClasses();
        for (String c : classes) {
            cbClassFilter.addItem(c);
        }

        if (current != null && classes.contains(current)) {
            cbClassFilter.setSelectedItem(current);
        } else {
            cbClassFilter.setSelectedIndex(0);
        }

        // Restore action listener
        for (java.awt.event.ActionListener l : listeners) {
            cbClassFilter.addActionListener(l);
        }
    }

    private boolean isUpdatingFilter = false;

    // --- HÀM CẬP NHẬT DỮ LIỆU ---
    public void refreshData() {
        if (isUpdatingFilter)
            return;

        if (cbClassFilter.getItemCount() == 0) {
            isUpdatingFilter = true;
            updateFilterOptions();
            isUpdatingFilter = false;
        }

        String selectedClass = (String) cbClassFilter.getSelectedItem();
        if (selectedClass == null)
            selectedClass = "Tất cả";

        // 1. Cập nhật KPI
        lblTotalStudents.setText(String.valueOf(dao.countTotalSinhVien(selectedClass)));
        lblTotalClasses.setText(String.valueOf(dao.countTotalClasses()));
        lblAvgScore.setText(String.format("%.2f", dao.getDiemTrungBinh(selectedClass)));

        Document ratio = dao.getTiLeNamNu(selectedClass);
        int nam = ratio.getInteger("Nam") != null ? ratio.getInteger("Nam") : 0;
        int nu = ratio.getInteger("Nữ") != null ? ratio.getInteger("Nữ") : 0;
        int total = nam + nu;
        if (total > 0) {
            lblGenderRatio.setText(String.format("%d%% / %d%%", (nam * 100) / total, (nu * 100) / total));
        } else {
            lblGenderRatio.setText("0% / 0%");
        }

        // 2. Cập nhật biểu đồ Tròn (PieChart - Nam/Nữ)
        pieChartGender.getSeriesMap().clear();
        if (nam > 0)
            pieChartGender.addSeries("Nam", nam);
        if (nu > 0)
            pieChartGender.addSeries("Nữ", nu);
        if (nam == 0 && nu == 0)
            pieChartGender.addSeries("Không có dữ liệu", 1);
        panelPieChart.revalidate();
        panelPieChart.repaint();

        // 3. Cập nhật biểu đồ Cột (BarChart - Ngoại ngữ)
        List<Document> langStats = dao.getThongKeNgoaiNgu(selectedClass);
        barChartLang.getSeriesMap().clear();
        List<String> xData = new ArrayList<>();
        List<Number> yData = new ArrayList<>();
        if (langStats.isEmpty()) {
            xData.add("Không có dữ liệu");
            yData.add(0);
        } else {
            for (Document doc : langStats) {
                xData.add(doc.getString("_id"));
                yData.add(doc.getInteger("count"));
            }
        }
        barChartLang.addSeries("Sinh viên", xData, yData);
        panelBarChart.revalidate();
        panelBarChart.repaint();

        // 4. Cập nhật bảng Top 5
        DefaultTableModel modelTop = (DefaultTableModel) tblTopStudents.getModel();
        modelTop.setRowCount(0);
        List<Document> topStudents = dao.getTop5SinhVien(selectedClass);
        for (Document doc : topStudents) {
            modelTop.addRow(new Object[] {
                    doc.getString("masv"),
                    doc.getString("hoten"),
                    doc.getString("malop"),
                    doc.get("diemTB") != null ? String.format("%.2f", doc.getDouble("diemTB")) : "0"
            });
        }

        // 5. Cập nhật bảng Thống kê Lớp (Luôn hiện toàn bộ)
        DefaultTableModel modelClass = (DefaultTableModel) tblClassStats.getModel();
        modelClass.setRowCount(0);
        List<Document> classStats = dao.getThongKeTheoLop();
        for (Document doc : classStats) {
            modelClass.addRow(new Object[] {
                    doc.getString("_id"),
                    doc.getInteger("siso"),
                    doc.get("diemTBCaoNhat") != null ? String.format("%.2f", doc.getDouble("diemTBCaoNhat")) : "0",
                    doc.get("diemTBThapNhat") != null ? String.format("%.2f", doc.getDouble("diemTBThapNhat")) : "0"
            });
        }

        // 6. Cập nhật bảng Phân loại học lực
        DefaultTableModel modelHocLuc = (DefaultTableModel) tblHocLuc.getModel();
        modelHocLuc.setRowCount(0);
        Document hlStats = dao.getPhanLoaiHocLuc(selectedClass);
        modelHocLuc.addRow(new Object[] { "Xuất sắc (>= 9.0)", hlStats.getInteger("Xuất sắc") });
        modelHocLuc.addRow(new Object[] { "Giỏi (8.0 - 8.9)", hlStats.getInteger("Giỏi") });
        modelHocLuc.addRow(new Object[] { "Khá (7.0 - 7.9)", hlStats.getInteger("Khá") });
        modelHocLuc.addRow(new Object[] { "Trung bình (5.0 - 6.9)", hlStats.getInteger("Trung bình") });
        modelHocLuc.addRow(new Object[] { "Yếu (< 5.0)", hlStats.getInteger("Yếu") });
    }
}
