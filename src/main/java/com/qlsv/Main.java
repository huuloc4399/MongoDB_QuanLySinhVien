package com.qlsv;

import com.formdev.flatlaf.FlatLightLaf;
import com.qlsv.config.MongoDbConnection;
import com.qlsv.ui.MainFrame;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.bson.Document;
import com.qlsv.dao.SinhVienDAO;
import com.mongodb.MongoWriteException;

public class Main {
    public static void main(String[] args) {
        // Thiết lập giao diện FlatLaf (Giao diện hiện đại)
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            // Cấu hình một số font chữ mặc định nếu cần
            UIManager.put("defaultFont", new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        } catch (Exception ex) {
            System.err.println("Không thể khởi tạo FlatLaf");
        }

        // Khởi tạo kết nối MongoDB (Singleton) ngay khi chạy để tạo Index
        MongoDbConnection.getInstance();

        // Optional: Import dữ liệu seed nếu collection trống
        seedDataIfEmpty();

        // Chạy ứng dụng trên Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });

        // Đảm bảo đóng kết nối khi tắt app
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            MongoDbConnection.getInstance().close();
        }));
    }

    private static void seedDataIfEmpty() {
        SinhVienDAO dao = new SinhVienDAO();
        if (dao.countTotalSinhVien(null) == 0) {
            System.out.println("Collection rỗng. Đang import dữ liệu từ data_seed.json...");
            try {
                String content = new String(Files.readAllBytes(Paths.get("data_seed.json")), "UTF-8");
                JSONArray jsonArray = new JSONArray(content);
                int count = 0;
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    Document doc = Document.parse(obj.toString());
                    try {
                        dao.addSinhVien(doc);
                        count++;
                    } catch (MongoWriteException e) {
                        // Bỏ qua lỗi duplicate key nếu có
                    }
                }
                System.out.println("Đã import thành công " + count + " sinh viên.");
            } catch (Exception e) {
                System.err.println("Lỗi khi import dữ liệu: " + e.getMessage());
            }
        }
    }
}
