package com.qlsv.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.bson.Document;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Lớp cấu hình kết nối MongoDB sử dụng Singleton Pattern
 * Đảm bảo chỉ có một instance kết nối được khởi tạo trong suốt vòng đời ứng dụng.
 */
public class MongoDbConnection {
    private static MongoDbConnection instance;
    private MongoClient mongoClient;
    private MongoDatabase database;

    private MongoDbConnection() {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            props.load(in);
            String uri = props.getProperty("mongodb.uri", "mongodb://localhost:27017");
            String dbName = props.getProperty("mongodb.database", "qlsinhvien_db");

            // Khởi tạo MongoClient
            this.mongoClient = MongoClients.create(uri);
            this.database = mongoClient.getDatabase(dbName);
            
            System.out.println("Kết nối MongoDB thành công: " + uri);
            
            // Khởi tạo các Index yêu cầu
            initIndexes();
        } catch (IOException e) {
            System.err.println("Không thể đọc cấu hình database: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Lỗi kết nối MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Lấy instance duy nhất của MongoDbConnection (Singleton)
     */
    public static synchronized MongoDbConnection getInstance() {
        if (instance == null) {
            instance = new MongoDbConnection();
        }
        return instance;
    }

    /**
     * Lấy đối tượng Database
     */
    public MongoDatabase getDatabase() {
        return database;
    }
    
    /**
     * Đóng kết nối (khi ứng dụng kết thúc)
     */
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("Đã đóng kết nối MongoDB.");
        }
    }

    /**
     * Khởi tạo các Index tự động khi ứng dụng chạy
     */
    private void initIndexes() {
        try {
            var collection = database.getCollection("sinhvien");
            
            // 1. Unique Index cho trường masv (ngăn chặn trùng lặp mã sinh viên)
            IndexOptions uniqueOptions = new IndexOptions().unique(true);
            collection.createIndex(Indexes.ascending("masv"), uniqueOptions);
            System.out.println("Đã tạo Unique Index cho 'masv'.");
            
            // 2. Compound Index cho cặp trường { malop: 1, hoten: 1 } (tối ưu hóa thao tác tìm kiếm và sắp xếp theo danh sách lớp)
            collection.createIndex(Indexes.ascending("malop", "hoten"));
            System.out.println("Đã tạo Compound Index cho {'malop': 1, 'hoten': 1}.");
            
        } catch (Exception e) {
            System.err.println("Lỗi khi tạo Index: " + e.getMessage());
        }
    }
}
