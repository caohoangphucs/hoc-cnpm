package vn.id.caohoangphuc.cnpm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.id.caohoangphuc.cnpm.dao.ReportDAO;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "ReportServlet", urlPatterns = {"/reports/dashboard"})
public class ReportServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/index.jsp");
            return;
        }

        List<Map<String, Object>> occupancy = ReportDAO.getOccupancyRate();
        List<Map<String, Object>> revenue = ReportDAO.getMonthlyRevenue();

        req.setAttribute("occupancy", occupancy);
        req.setAttribute("revenue", revenue);
        req.getRequestDispatcher("/WEB-INF/views/reports/dashboard.jsp").forward(req, resp);
    }
}
