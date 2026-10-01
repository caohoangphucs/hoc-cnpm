package vn.id.caohoangphuc.cnpm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.id.caohoangphuc.cnpm.dao.MaintenanceDAO;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "MaintenanceServlet", urlPatterns = {"/maintenance/dashboard", "/maintenance/complete-repair"})
public class MaintenanceServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/index.jsp");
            return;
        }

        List<Map<String, Object>> supplies = MaintenanceDAO.getSupplies();
        List<Map<String, Object>> requests = MaintenanceDAO.getRepairRequests();

        req.setAttribute("supplies", supplies);
        req.setAttribute("requests", requests);
        req.getRequestDispatcher("/WEB-INF/views/maintenance/dashboard.jsp").forward(req, resp);
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

        if ("/maintenance/complete-repair".equals(path)) {
            int reqId = Integer.parseInt(req.getParameter("requestId"));
            String supplyId = req.getParameter("supplyId");
            int quantity = Integer.parseInt(req.getParameter("quantity"));
            String note = req.getParameter("note");

            Map<String, Object> res = MaintenanceDAO.completeRepair(reqId, supplyId, quantity, username, note != null ? note : "");
            session.setAttribute("toastMsg", res.get("message"));
            session.setAttribute("toastType", "SUCCESS".equals(res.get("status")) ? "success" : "danger");
            resp.sendRedirect(req.getContextPath() + "/maintenance/dashboard");
        }
    }
}
