package vn.id.caohoangphuc.cnpm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.id.caohoangphuc.cnpm.dao.ReportDAO;
import vn.id.caohoangphuc.cnpm.dao.RoomDAO;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "AdminServlet", urlPatterns = {"/admin/dashboard", "/admin/registrations", "/admin/approve-registration"})
public class AdminServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/index.jsp");
            return;
        }

        String path = req.getServletPath();
        if ("/admin/registrations".equals(path)) {
            List<Map<String, Object>> pending = RoomDAO.getPendingRegistrations();
            req.setAttribute("pendingList", pending);
            req.getRequestDispatcher("/WEB-INF/views/admin/registrations.jsp").forward(req, resp);
        } else {
            // Dashboard
            List<Map<String, Object>> pending = RoomDAO.getPendingRegistrations();
            List<Map<String, Object>> occupancy = ReportDAO.getOccupancyRate();
            req.setAttribute("pendingList", pending);
            req.setAttribute("occupancy", occupancy);
            req.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(req, resp);
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
        if ("/admin/approve-registration".equals(path)) {
            int regId = Integer.parseInt(req.getParameter("registrationId"));
            String decision = req.getParameter("decision"); // 'DUYET' hoặc 'TU_CHOI'
            String note = req.getParameter("note");

            Map<String, Object> res = RoomDAO.approveRegistration(regId, decision, note != null ? note : "");
            session.setAttribute("toastMsg", res.get("message"));
            session.setAttribute("toastType", "SUCCESS".equals(res.get("status")) ? "success" : "danger");
            resp.sendRedirect(req.getContextPath() + "/admin/registrations");
        }
    }
}
