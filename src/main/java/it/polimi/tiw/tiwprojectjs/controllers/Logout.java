package it.polimi.tiw.tiwprojectjs.controllers;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@WebServlet(name = "Logout", value = "/Logout")
public class Logout extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession();
        if (session.isNew() || session.getAttribute("user") == null) {

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().println("No user logged");
            return;
        }

        request.getSession().removeAttribute("user");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("user logged out");
    }
}
