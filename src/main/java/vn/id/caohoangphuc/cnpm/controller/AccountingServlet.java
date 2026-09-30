package vn.id.caohoangphuc.cnpm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.id.caohoangphuc.cnpm.dao.InvoiceDAO;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "AccountingServlet", urlPatterns = {"/accounting/dashboard", "/accounting/meters", "/accounting/generate-invoice", "/accounting/confirm-payment"})
public class AccountingServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/index.jsp");
            return;
        }

        List<Map<String, Object>> invoices = InvoiceDAO.getAllInvoices();
        req.setAttribute("invoices", invoices);
        req.getRequestDispatcher("/WEB-INF/views/accounting/dashboard.jsp").forward(req, resp);
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

        if ("/accounting/meters".equals(path)) {
            String roomId = req.getParameter("roomId");
            String month = req.getParameter("month");
            int oldE = Integer.parseInt(req.getParameter("oldE"));
            int newE = Integer.parseInt(req.getParameter("newE"));
            int oldW = Integer.parseInt(req.getParameter("oldW"));
            int newW = Integer.parseInt(req.getParameter("newW"));

            Map<String, Object> res = InvoiceDAO.recordMeter(roomId, month, oldE, newE, oldW, newW, username);
            session.setAttribute("toastMsg", res.get("message"));
            session.setAttribute("toastType", "SUCCESS".equals(res.get("status")) ? "success" : "danger");
            resp.sendRedirect(req.getContextPath() + "/accounting/dashboard");
        } else if ("/accounting/generate-invoice".equals(path)) {
            String roomId = req.getParameter("roomId");
            String month = req.getParameter("month");

            Map<String, Object> res = InvoiceDAO.generateInvoice(roomId, month);
            session.setAttribute("toastMsg", res.get("message"));
            session.setAttribute("toastType", "SUCCESS".equals(res.get("status")) ? "success" : "danger");
            resp.sendRedirect(req.getContextPath() + "/accounting/dashboard");
        } else if ("/accounting/confirm-payment".equals(path)) {
            String txnRef = req.getParameter("txnRef");
            String method = req.getParameter("method");
            double amount = Double.parseDouble(req.getParameter("amount"));

            Map<String, Object> res = InvoiceDAO.confirmPayment(txnRef, method, amount, username);
            session.setAttribute("toastMsg", res.get("message"));
            session.setAttribute("toastType", "SUCCESS".equals(res.get("status")) ? "success" : "danger");
            resp.sendRedirect(req.getContextPath() + "/accounting/dashboard");
        }
    }
}
