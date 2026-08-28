package com.qlsv.dao;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.qlsv.config.MongoDbConnection;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Lớp DAO thực hiện các thao tác CRUD và Aggregation với Collection 'sinhvien'
 */
public class SinhVienDAO {
    private final MongoCollection<Document> collection;

    public SinhVienDAO() {
        // Lấy collection thông qua Singleton connection
        this.collection = MongoDbConnection.getInstance().getDatabase().getCollection("sinhvien");
    }

    // ==========================================
    // III.3: THAO TÁC CRUD CƠ BẢN
    // ==========================================

    /**
     * Thêm 1 sinh viên mới (Create)
     * @param doc Thông tin sinh viên dưới dạng Document
     */
    public void addSinhVien(Document doc) {
        collection.insertOne(doc);
    }

    /**
     * Tải toàn bộ danh sách sinh viên
     */
    public List<Document> getAllSinhVien() {
        return collection.find().into(new ArrayList<>());
    }

    /**
     * Tìm kiếm sinh viên chính xác theo masv
     * @param masv Mã sinh viên cần tìm
     */
    public Document getSinhVienByMasv(String masv) {
        return collection.find(Filters.eq("masv", masv)).first();
    }

    /**
     * Lọc danh sách sinh viên theo malop
     * @param malop Mã lớp cần lọc
     */
    public List<Document> getSinhVienByLop(String malop) {
        return collection.find(Filters.eq("malop", malop)).into(new ArrayList<>());
    }

    /**
     * Tìm kiếm sinh viên gần đúng (theo masv, hoten, malop) và sắp xếp
     * @param keyword Từ khóa tìm kiếm
     * @param sortType Kiểu sắp xếp (Mặc định, Mã SV, Họ tên, Lớp)
     */
    public List<Document> searchSinhVien(String keyword, String sortType) {
        Bson filter = new Document();
        if (keyword != null && !keyword.trim().isEmpty()) {
            filter = Filters.or(
                    Filters.regex("masv", keyword, "i"),
                    Filters.regex("hoten", keyword, "i"),
                    Filters.regex("malop", keyword, "i")
            );
        }
        
        Bson sort = null;
        if ("Mã SV".equals(sortType)) {
            sort = Sorts.ascending("masv");
        } else if ("Họ tên".equals(sortType)) {
            sort = Sorts.ascending("hoten");
        } else if ("Lớp".equals(sortType)) {
            sort = Sorts.ascending("malop");
        }
        
        if (sort != null) {
            return collection.find(filter).sort(sort).into(new ArrayList<>());
        } else {
            return collection.find(filter).into(new ArrayList<>());
        }
    }

    /**
     * Cập nhật thông tin cơ bản: Họ tên, Tuổi, Giới tính, Mã lớp
     * @param masv Mã sinh viên
     * @param hoten Họ tên mới
     * @param tuoi Tuổi mới
     * @param phai Giới tính mới
     * @param malop Mã lớp mới
     */
    public boolean updateThongTinCoBan(String masv, String hoten, int tuoi, String phai, String malop) {
        Bson filter = Filters.eq("masv", masv);
        Bson update = Updates.combine(
                Updates.set("hoten", hoten),
                Updates.set("tuoi", tuoi),
                Updates.set("phai", phai),
                Updates.set("malop", malop)
        );
        UpdateResult result = collection.updateOne(filter, update);
        return result.getModifiedCount() > 0;
    }

    /**
     * Xóa 1 sinh viên được chọn theo masv
     * @param masv Mã sinh viên
     */
    public boolean deleteSinhVien(String masv) {
        DeleteResult result = collection.deleteOne(Filters.eq("masv", masv));
        return result.getDeletedCount() > 0;
    }

    /**
     * Xóa toàn bộ sinh viên thuộc một lớp cụ thể
     * @param malop Mã lớp
     */
    public long deleteSinhVienByLop(String malop) {
        DeleteResult result = collection.deleteMany(Filters.eq("malop", malop));
        return result.getDeletedCount();
    }

    // ==========================================
    // III.4: XỬ LÝ MẢNG NÂNG CAO & THAY THẾ DOCUMENT
    // ==========================================

    /**
     * Thêm một ngoại ngữ mới vào mảng ngoaingu ($addToSet để tránh trùng lặp)
     */
    public boolean addNgoaiNgu(String masv, String ngoaingu) {
        Bson filter = Filters.eq("masv", masv);
        Bson update = Updates.addToSet("ngoaingu", ngoaingu);
        UpdateResult result = collection.updateOne(filter, update);
        return result.getModifiedCount() > 0;
    }

    /**
     * Sửa một ngoại ngữ trong mảng ngoaingu
     */
    public boolean updateNgoaiNgu(String masv, String oldNgoaiNgu, String newNgoaiNgu) {
        Bson filter = Filters.and(
                Filters.eq("masv", masv),
                Filters.eq("ngoaingu", oldNgoaiNgu)
        );
        Bson update = Updates.set("ngoaingu.$", newNgoaiNgu);
        UpdateResult result = collection.updateOne(filter, update);
        return result.getModifiedCount() > 0;
    }

    /**
     * Xóa một ngoại ngữ trong mảng ngoaingu ($pull)
     */
    public boolean deleteNgoaiNgu(String masv, String ngoaingu) {
        Bson filter = Filters.eq("masv", masv);
        Bson update = Updates.pull("ngoaingu", ngoaingu);
        UpdateResult result = collection.updateOne(filter, update);
        return result.getModifiedCount() > 0;
    }

    /**
     * Thêm một môn học mới vào mảng monhoc ($push)
     */
    public boolean addMonHoc(String masv, Document monhocDoc) {
        Bson filter = Filters.eq("masv", masv);
        Bson update = Updates.push("monhoc", monhocDoc);
        UpdateResult result = collection.updateOne(filter, update);
        return result.getModifiedCount() > 0;
    }

    /**
     * Cập nhật điểm số của một môn học cụ thể (Positional Operator $)
     */
    public boolean updateDiemMonHoc(String masv, String mamon, double diemMoi) {
        // Tìm document có masv và trong mảng monhoc có môn học mang mã mamon
        Bson filter = Filters.and(
                Filters.eq("masv", masv),
                Filters.eq("monhoc.mamon", mamon)
        );
        // Cập nhật giá trị điểm của môn học đó bằng Positional Operator
        Bson update = Updates.set("monhoc.$.diem", diemMoi);
        UpdateResult result = collection.updateOne(filter, update);
        return result.getModifiedCount() > 0;
    }

    /**
     * Sửa tên môn và điểm của môn học
     */
    public boolean updateMonHoc(String masv, String mamon, String tenmonMoi, double diemMoi) {
        Bson filter = Filters.and(
                Filters.eq("masv", masv),
                Filters.eq("monhoc.mamon", mamon)
        );
        Bson update = Updates.combine(
                Updates.set("monhoc.$.tenmon", tenmonMoi),
                Updates.set("monhoc.$.diem", diemMoi)
        );
        UpdateResult result = collection.updateOne(filter, update);
        return result.getModifiedCount() > 0;
    }

    /**
     * Xóa một môn học trong mảng monhoc ($pull)
     */
    public boolean deleteMonHoc(String masv, String mamon) {
        Bson filter = Filters.eq("masv", masv);
        Bson update = Updates.pull("monhoc", new Document("mamon", mamon));
        UpdateResult result = collection.updateOne(filter, update);
        return result.getModifiedCount() > 0;
    }

    /**
     * Thay thế toàn bộ document sinh viên theo _id (replaceOne)
     */
    public boolean replaceDocument(Document originalDoc, Document newDoc) {
        Object id = originalDoc.getObjectId("_id");
        Bson filter = Filters.eq("_id", id);
        UpdateResult result = collection.replaceOne(filter, newDoc);
        return result.getModifiedCount() > 0;
    }

    // ==========================================
    // III.5: DASHBOARD & BÁO CÁO THỐNG KÊ (AGGREGATION)
    // ==========================================

    /**
     * Lấy danh sách tất cả các lớp học
     */
    public List<String> getAllClasses() {
        return collection.distinct("malop", String.class).into(new ArrayList<>());
    }

    /**
     * Helper tạo match stage
     */
    private List<Bson> buildPipelineWithMatch(String malop, Bson... stages) {
        List<Bson> pipeline = new ArrayList<>();
        if (malop != null && !malop.equals("Tất cả")) {
            pipeline.add(Aggregates.match(Filters.eq("malop", malop)));
        }
        pipeline.addAll(Arrays.asList(stages));
        return pipeline;
    }

    /**
     * Tổng số sinh viên
     */
    public long countTotalSinhVien(String malop) {
        if (malop != null && !malop.equals("Tất cả")) {
            return collection.countDocuments(Filters.eq("malop", malop));
        }
        return collection.countDocuments();
    }

    /**
     * Tổng số lớp học khác nhau
     */
    public int countTotalClasses() {
        return getAllClasses().size();
    }

    /**
     * Tính điểm trung bình toàn trường / lớp
     */
    public double getDiemTrungBinh(String malop) {
        List<Bson> pipeline = buildPipelineWithMatch(malop,
                Aggregates.unwind("$monhoc"),
                Aggregates.group(null, Accumulators.avg("avgDiem", "$monhoc.diem"))
        );
        AggregateIterable<Document> result = collection.aggregate(pipeline);
        Document doc = result.first();
        return doc != null && doc.get("avgDiem") != null ? doc.getDouble("avgDiem") : 0.0;
    }

    /**
     * Tính tỉ lệ Nam / Nữ
     */
    public Document getTiLeNamNu(String malop) {
        List<Bson> pipeline = buildPipelineWithMatch(malop,
                Aggregates.group("$phai", Accumulators.sum("count", 1))
        );
        AggregateIterable<Document> result = collection.aggregate(pipeline);
        Document stats = new Document();
        for (Document doc : result) {
            String phai = doc.getString("_id");
            int count = doc.getInteger("count");
            stats.put(phai, count);
        }
        return stats;
    }

    /**
     * Thống kê sinh viên theo lớp (Mã lớp, sĩ số, ĐTB cao nhất, ĐTB thấp nhất)
     */
    public List<Document> getThongKeTheoLop() {
        // Cần tính điểm TB của từng sinh viên trước, sau đó group theo lớp
        return collection.aggregate(Arrays.asList(
                // Tính điểm TB mỗi sinh viên
                new Document("$addFields", new Document("diemTB", new Document("$avg", "$monhoc.diem"))),
                // Group theo lớp
                Aggregates.group("$malop", 
                        Accumulators.sum("siso", 1),
                        Accumulators.max("diemTBCaoNhat", "$diemTB"),
                        Accumulators.min("diemTBThapNhat", "$diemTB")
                ),
                Aggregates.sort(Sorts.ascending("_id"))
        )).into(new ArrayList<>());
    }

    /**
     * Thống kê ngoại ngữ phổ biến
     */
    public List<Document> getThongKeNgoaiNgu(String malop) {
        List<Bson> pipeline = buildPipelineWithMatch(malop,
                Aggregates.unwind("$ngoaingu"),
                Aggregates.group("$ngoaingu", Accumulators.sum("count", 1)),
                Aggregates.sort(Sorts.descending("count"))
        );
        return collection.aggregate(pipeline).into(new ArrayList<>());
    }

    /**
     * Top 5 Sinh viên có điểm TB cao nhất
     */
    public List<Document> getTop5SinhVien(String malop) {
        List<Bson> pipeline = buildPipelineWithMatch(malop,
                new Document("$addFields", new Document("diemTB", new Document("$avg", "$monhoc.diem"))),
                Aggregates.sort(Sorts.descending("diemTB")),
                Aggregates.limit(5),
                Aggregates.project(Projections.fields(
                        Projections.include("masv", "hoten", "malop", "diemTB"),
                        Projections.excludeId()
                ))
        );
        return collection.aggregate(pipeline).into(new ArrayList<>());
    }

    /**
     * Thống kê phân loại học lực
     * Xuất sắc: >= 9.0
     * Giỏi: 8.0 - 8.9
     * Khá: 7.0 - 7.9
     * Trung bình: 5.0 - 6.9
     * Yếu: < 5.0
     */
    public Document getPhanLoaiHocLuc(String malop) {
        List<Bson> pipeline = buildPipelineWithMatch(malop,
                new Document("$addFields", new Document("diemTB", new Document("$avg", "$monhoc.diem")))
        );
        AggregateIterable<Document> result = collection.aggregate(pipeline);
        
        int xuatSac = 0, gioi = 0, kha = 0, trungBinh = 0, yeu = 0;
        for (Document doc : result) {
            Double diemTB = null;
            Object diemObj = doc.get("diemTB");
            if (diemObj instanceof Number) {
                diemTB = ((Number) diemObj).doubleValue();
            }
            if (diemTB == null) diemTB = 0.0;
            
            if (diemTB >= 9.0) xuatSac++;
            else if (diemTB >= 8.0) gioi++;
            else if (diemTB >= 7.0) kha++;
            else if (diemTB >= 5.0) trungBinh++;
            else yeu++;
        }
        
        Document stats = new Document();
        stats.put("Xuất sắc", xuatSac);
        stats.put("Giỏi", gioi);
        stats.put("Khá", kha);
        stats.put("Trung bình", trungBinh);
        stats.put("Yếu", yeu);
        return stats;
    }

    /**
     * Thống kê theo mã môn và tên môn (Sử dụng unwind, match, group, project)
     * @param keyword Từ khóa tìm kiếm (có thể là mã môn, tên môn, hoặc để rỗng để lấy tất cả)
     */
    public List<Document> getThongKeMaMonVaTenMon(String keyword) {
        Bson matchCondition;
        if (keyword != null && !keyword.trim().isEmpty()) {
            matchCondition = Filters.or(
                    Filters.regex("monhoc.mamon", keyword, "i"),
                    Filters.regex("monhoc.tenmon", keyword, "i")
            );
        } else {
            // Match cơ bản để đảm bảo môn học tồn tại
            matchCondition = Filters.exists("monhoc.mamon");
        }

        List<Bson> pipeline = Arrays.asList(
                Aggregates.unwind("$monhoc"),
                Aggregates.match(matchCondition),
                Aggregates.group(
                        new Document("mamon", "$monhoc.mamon").append("tenmon", "$monhoc.tenmon"),
                        Accumulators.sum("soLuongSinhVien", 1),
                        Accumulators.avg("diemTrungBinh", "$monhoc.diem")
                ),
                Aggregates.project(Projections.fields(
                        Projections.computed("mamon", "$_id.mamon"),
                        Projections.computed("tenmon", "$_id.tenmon"),
                        Projections.include("soLuongSinhVien", "diemTrungBinh"),
                        Projections.excludeId()
                )),
                Aggregates.sort(Sorts.ascending("mamon"))
        );
        return collection.aggregate(pipeline).into(new ArrayList<>());
    }
}
