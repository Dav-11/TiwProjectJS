package it.polimi.tiw.tiwprojectjs.controllers;

import com.google.gson.Gson;
import it.polimi.tiw.tiwprojectjs.beans.DashboardAuction;
import it.polimi.tiw.tiwprojectjs.dao.DashboardAuctionDAO;
import it.polimi.tiw.tiwprojectjs.utils.ConnectionHandler;
import org.apache.commons.lang3.StringEscapeUtils;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "SearchAuction", value = "/SearchAuction")
@MultipartConfig
public class SearchAuction extends HttpServlet {

    private Connection connection = null;

    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    /**
     * params:
     * keystring -> required
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


        HttpSession session = request.getSession();
        if (session.isNew() || session.getAttribute("user") == null) {

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().println("unauthorized user");
            return;
        } else {

            String keystring = null;

            try {

                keystring = StringEscapeUtils.escapeJava(request.getParameter("keystring"));
            } catch ( NullPointerException e){

                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("Incorrect or missing param values");
                return;
            }

            // get a new list with the searched keystring

            DashboardAuctionDAO dashboardAuctionDAO = new DashboardAuctionDAO(connection);
            List<DashboardAuction> auctionList = new ArrayList<>();

            try {

                auctionList = dashboardAuctionDAO.getListOfOpenAuctionByKeystring(keystring);
            } catch (SQLException e){

                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().println("Unable to recover Auction's list");
                return;
            }

            String json = new Gson().toJson(auctionList);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);
        }
    }

    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
