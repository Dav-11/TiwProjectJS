package it.polimi.tiw.tiwprojectjs.controllers;

import com.google.gson.Gson;
import it.polimi.tiw.tiwprojectjs.beans.Auction;
import it.polimi.tiw.tiwprojectjs.beans.DashboardAuction;
import it.polimi.tiw.tiwprojectjs.beans.DashboardAuctionToGo;
import it.polimi.tiw.tiwprojectjs.dao.AuctionDAO;
import it.polimi.tiw.tiwprojectjs.dao.DashboardAuctionDAO;
import it.polimi.tiw.tiwprojectjs.utils.ConnectionHandler;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet(name = "GetAuctionDetail", value = "/GetAuctionDetail")
public class GetAuctionDetail extends HttpServlet {

    private Connection connection = null;

    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    /**
     * params
     * auction_id -> required
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession();
        if (session.isNew() || session.getAttribute("user") == null) {

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().println("unauthorized user");
            return;
        }

        // Get parameters
        Integer auctionId = null;

        try{

            auctionId = Integer.parseInt(request.getParameter("auction_id"));
        } catch (NumberFormatException | NullPointerException e){

            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Incorrect or missing param values");
            return;
        }

        DashboardAuctionDAO dashboardAuctionDAO = new DashboardAuctionDAO(connection);
        DashboardAuction auction =null;

        try {

            auction = dashboardAuctionDAO.getAuctionDetail(auctionId);
        } catch (SQLException e){

            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Unable to recover Auction's detail");
            return;
        }

        if (auction == null){

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Auction not found");
            return;
        }

        // return a json of the auction
        String json = new Gson().toJson(new DashboardAuctionToGo(auction));
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }
}
