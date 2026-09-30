package vn.id.caohoangphuc.cnpm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.id.caohoangphuc.cnpm.dao.SecurityDAO;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "SecurityServlet", urlPatterns = {"/security/dashboard", "/security/record-entry"})
public class SecurityServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/index.jsp");
            return;
        }

        List<Map<String, Object>> logs = SecurityDAO.getEntryLogs();
        List<Map<String, Object>> violations = SecurityDAO.getViolations();

        req.setAttribute("logs", logs);
        req.setAttribute("violations", violations);
        req.getRequestDispatcher("/WEB-INF/views/security/dashboard.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/index.jsp");
            return;
        }

        String path = req.getServletPath();
        String username = (String) session.getAttribute("username");

        if ("/security/record-entry".equals(path)) {
            String studentId = req.getParameter("studentId");
            String type = req.getParameter("type"); // VAO / RA
            String note = req.getParameter("note");

            Map<String, Object> res = SecurityDAO.recordEntry(studentId, type, username, note != null ? note : "");
            session.setAttribute("toastMsg", res.get("message"));
            session.setAttribute("toastType", "VIOLATION_RECORDED".equals(res.get("status")) ? "warning" : "success");
            resp.sendRedirect(req.getContextPath() + "/security/dashboard");
        }
    }
}
