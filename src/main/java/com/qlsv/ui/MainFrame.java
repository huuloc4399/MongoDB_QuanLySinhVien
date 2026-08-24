package com.qlsv.ui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private JTabbedPane tabbedPane;
    private StudentPanel studentPanel;
    private DashboardPanel dashboardPanel;

    public MainFrame() {
        setTitle("Hệ thống Quản lý Sinh viên - MongoDB");
        setSize(1024, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen

        initComponents();
    }

    private void initComponents() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

        studentPanel = new StudentPanel();
        dashboardPanel = new DashboardPanel();

        tabbedPane.addTab("Quản lý Sinh viên", new ImageIcon(), studentPanel, "Thao tác CRUD Sinh viên");
        tabbedPane.addTab("Dashboard & Thống kê", new ImageIcon(), dashboardPanel, "Báo cáo thống kê tổng quan");

        // Khi chuyển tab, làm mới dữ liệu
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 1) {
                dashboardPanel.refreshData(); // Cập nhật dashboard khi mở tab
            } else {
                studentPanel.refreshTable();
            }
        });

        add(tabbedPane, BorderLayout.CENTER);
    }
}
