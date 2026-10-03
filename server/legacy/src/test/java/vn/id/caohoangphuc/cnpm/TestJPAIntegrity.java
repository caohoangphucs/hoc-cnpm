package vn.id.caohoangphuc.cnpm;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import vn.id.caohoangphuc.cnpm.config.JPAUtil;
import vn.id.caohoangphuc.cnpm.entity.*;

import java.util.List;

public class TestJPAIntegrity {
    public static void main(String[] args) {
        System.out.println("=== BAT DAU KIEM TRA JPA INTEGRITY & ENTITIES ===");
        try {
            EntityManager em = JPAUtil.getEntityManager();

            // 1. Test SinhVien & Phong
            TypedQuery<SinhVien> svQuery = em.createQuery("SELECT s FROM SinhVien s", SinhVien.class);
            List<SinhVien> svList = svQuery.getResultList();
            System.out.println("-> So luong SinhVien tim thay qua JPA: " + svList.size());

            // 2. Test ToaNha & Phong
            TypedQuery<ToaNha> toaQuery = em.createQuery("SELECT t FROM ToaNha t", ToaNha.class);
            List<ToaNha> toaList = toaQuery.getResultList();
            System.out.println("-> So luong ToaNha tim thay qua JPA: " + toaList.size());

            // 3. Test HoaDon
            TypedQuery<HoaDon> hdQuery = em.createQuery("SELECT h FROM HoaDon h", HoaDon.class);
            List<HoaDon> hdList = hdQuery.getResultList();
            System.out.println("-> So luong HoaDon tim thay qua JPA: " + hdList.size());

            // 4. Test HoatDong & DangKyCongIch
            TypedQuery<HoatDong> hdongQuery = em.createQuery("SELECT hd FROM HoatDong hd", HoatDong.class);
            List<HoatDong> hdongList = hdongQuery.getResultList();
            System.out.println("-> So luong HoatDong tim thay qua JPA: " + hdongList.size());

            em.close();
            JPAUtil.close();
            System.out.println("=== JPA INTEGRITY TEST THANH CONG 100% ===");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
