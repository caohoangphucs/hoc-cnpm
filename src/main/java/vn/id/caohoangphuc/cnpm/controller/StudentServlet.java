package vn.id.caohoangphuc.cnpm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.id.caohoangphuc.cnpm.config.DBContext;
import vn.id.caohoangphuc.cnpm.dao.InvoiceDAO;
import vn.id.caohoangphuc.cnpm.dao.RoomDAO;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;

@WebServlet(name = "StudentServlet", urlPatterns = {"/student/dashboard", "/student/rooms", "/student/book-bed", "/student/invoices", "/student/repair-request"})
public class StudentServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/index.jsp");
            return;
        }

        String path = req.getServletPath();
        String studentId = (String) session.getAttribute("studentId");
        String roomId = (String) session.getAttribute("roomId");

        if ("/student/rooms".equals(path)) {
            List<Map<String, Object>> rooms = RoomDAO.getAvailableRooms();
            req.setAttribute("rooms", rooms);
            req.getRequestDispatcher("/WEB-INF/views/student/rooms.jsp").forward(req, resp);
        } else if ("/student/invoices".equals(path)) {
            if (roomId != null) {
                List<Map<String, Object>> invoices = InvoiceDAO.getInvoicesByRoom(roomId);
                req.setAttribute("invoices", invoices);
            }
            req.getRequestDispatcher("/WEB-INF/views/student/invoices.jsp").forward(req, resp);
        } else {
            // Dashboard
            List<Map<String, Object>> rooms = RoomDAO.getAvailableRooms();
            req.setAttribute("rooms", rooms);
            if (roomId != null) {
                List<Map<String, Object>> invoices = InvoiceDAO.getInvoicesByRoom(roomId);
                req.setAttribute("invoices", invoices);
            }
            req.getRequestDispatcher("/WEB-INF/views/student/dashboard.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/index.jsp");
            return;
        }

        String path = req.getServletPath();
        String studentId = (String) session.getAttribute("studentId");

        if ("/student/book-bed".equals(path)) {
            String bedId = req.getParameter("bedId");
            Map<String, Object> result = RoomDAO.bookBed(studentId, bedId);
            session.setAttribute("toastMsg", result.get("message"));
            session.setAttribute("toastType", "SUCCESS".equals(result.get("status")) ? "success" : "danger");
            resp.sendRedirect(req.getContextPath() + "/student/rooms");
        } else if ("/student/repair-request".equals(path)) {
            String roomId = (String) session.getAttribute("roomId");
            String title = req.getParameter("title");
            String desc = req.getParameter("description");
            String priority = req.getParameter("priority");

            String sql = "INSERT INTO YEU_CAU_SUA_CHUA (MaPhong, MaSinhVien, TieuDe, MoTaChiTiet, MucDoUuTien, TrangThai) VALUES (?, ?, ?, ?, ?, 'MOI')";
            try (Connection conn = DBContext.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, roomId != null ? roomId : "A-101");
                ps.setString(2, studentId != null ? studentId : "21110101");
                ps.setString(3, title);
                ps.setString(4, desc);
                ps.setString(5, priority);
                ps.executeUpdate();
                session.setAttribute("toastMsg", "Gửi yêu cầu báo hỏng / sửa chữa thành công!");
                session.setAttribute("toastType", "success");
            } catch (Exception e) {
                e.printStackTrace();
                session.setAttribute("toastMsg", "Lỗi: " + e.getMessage());
                session.setAttribute("toastType", "danger");
            }
            resp.sendRedirect(req.getContextPath() + "/student/dashboard");
        }
    }
}
