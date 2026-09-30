package vn.id.caohoangphuc.cnpm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.id.caohoangphuc.cnpm.dao.AccountDAO;

import java.io.IOException;
import java.util.Map;

@WebServlet(name = "AuthServlet", urlPatterns = {"/auth/login", "/auth/logout"})
public class AuthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        if ("/auth/logout".equals(path)) {
            HttpSession session = req.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            resp.sendRedirect(req.getContextPath() + "/index.jsp?logout=1");
            return;
        }
        resp.sendRedirect(req.getContextPath() + "/index.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String user = req.getParameter("username");
        String pass = req.getParameter("password");

        Map<String, Object> result = AccountDAO.login(user, pass);
        if ("SUCCESS".equals(result.get("status"))) {
            HttpSession session = req.getSession();
            session.setAttribute("user", result);
            session.setAttribute("role", result.get("role"));
            session.setAttribute("username", result.get("username"));
            session.setAttribute("fullName", result.get("fullName"));
            session.setAttribute("studentId", result.get("studentId"));
            session.setAttribute("roomId", result.get("roomId"));

            String role = (String) result.get("role");
            switch (role) {
                case "ADMIN":
                    resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
                    break;
                case "KETOAN":
                    resp.sendRedirect(req.getContextPath() + "/accounting/dashboard");
                    break;
                case "KYTHUAT":
                    resp.sendRedirect(req.getContextPath() + "/maintenance/dashboard");
                    break;
                case "BAOVE":
                    resp.sendRedirect(req.getContextPath() + "/security/dashboard");
                    break;
                case "SINHVIEN":
                default:
                    resp.sendRedirect(req.getContextPath() + "/student/dashboard");
                    break;
            }
        } else {
            req.setAttribute("error", result.get("message"));
            req.getRequestDispatcher("/index.jsp").forward(req, resp);
        }
    }
}
