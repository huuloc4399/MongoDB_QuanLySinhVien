import json
import random

first_names = ["An", "Bình", "Cường", "Duy", "Hải", "Tuấn", "Minh", "Huy", "Khang", "Nam", "Nghĩa", "Phát", "Thắng", "Thành", "Tài", "Đức", "Trí", "Việt", "Anh", "Hoa", "Lan", "Mai", "Ngọc", "Phương", "Quỳnh", "Thảo", "Trang", "Uyên", "Vân", "Yến", "Hà", "Hương"]
last_names = ["Nguyễn", "Trần", "Lê", "Phạm", "Hoàng", "Huỳnh", "Phan", "Vũ", "Võ", "Đặng", "Bùi", "Đỗ", "Hồ", "Ngô", "Dương", "Lý"]
middle_names = ["Văn", "Thị", "Hữu", "Hoàng", "Minh", "Thanh", "Đức", "Ngọc", "Gia", "Bảo", "Đình", "Quang"]
classes = ["CNTT1", "CNTT2", "KTPM1", "KTPM2", "HTTT1", "KHMT1"]
languages = ["Tiếng Anh", "Tiếng Nhật", "Tiếng Hàn", "Tiếng Trung", "Tiếng Pháp"]
subjects = [
    {"mamon": "csdl", "tenmon": "Cơ sở dữ liệu"},
    {"mamon": "laptrinh", "tenmon": "Lập trình Cơ bản"},
    {"mamon": "mang", "tenmon": "Mạng máy tính"},
    {"mamon": "hdt", "tenmon": "Lập trình Hướng đối tượng"},
    {"mamon": "ctdl", "tenmon": "Cấu trúc Dữ liệu"}
]

students = []
for i in range(1, 105):
    masv = f"sv{i:03d}"
    gender = random.choice(["Nam", "Nữ"])
    
    if gender == "Nam":
        middle = random.choice(["Văn", "Hữu", "Đức", "Đình", "Quang"])
    else:
        middle = random.choice(["Thị", "Ngọc", "Thanh", "Bích"])
        
    hoten = f"{random.choice(last_names)} {middle} {random.choice(first_names)}"
    
    age = random.randint(18, 23)
    malop = random.choice(classes)
    
    # 0 to 3 languages
    num_langs = random.randint(0, 3)
    ngoaingu = random.sample(languages, num_langs)
    
    # 1 to 5 subjects
    num_subs = random.randint(1, 5)
    subs_selected = random.sample(subjects, num_subs)
    
    monhoc = []
    for sub in subs_selected:
        score = round(random.uniform(3.0, 10.0), 1)
        monhoc.append({
            "mamon": sub["mamon"],
            "tenmon": sub["tenmon"],
            "diem": score
        })
        
    student = {
        "masv": masv,
        "hoten": hoten,
        "tuoi": age,
        "phai": gender,
        "malop": malop,
        "ngoaingu": ngoaingu,
        "monhoc": monhoc
    }
    students.append(student)

with open("data_seed.json", "w", encoding="utf-8") as f:
    json.dump(students, f, ensure_ascii=False, indent=2)

print("Generated data_seed.json with", len(students), "records")
