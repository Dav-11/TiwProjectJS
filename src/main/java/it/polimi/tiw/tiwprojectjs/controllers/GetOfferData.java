package it.polimi.tiw.tiwprojectjs.controllers;

import com.google.gson.Gson;
import it.polimi.tiw.tiwprojectjs.beans.DashboardAuction;
import it.polimi.tiw.tiwprojectjs.beans.Offer;
import it.polimi.tiw.tiwprojectjs.beans.User;
import it.polimi.tiw.tiwprojectjs.dao.DashboardAuctionDAO;
import it.polimi.tiw.tiwprojectjs.dao.OfferDAO;
import it.polimi.tiw.tiwprojectjs.utils.ConnectionHandler;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "GetOfferData", value = "/GetOfferData")
public class GetOfferData extends HttpServlet {

    private Connection connection = null;

    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    /**
     * params:
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

        // Get parameters from request
        Integer auctionId = null;

        try{
            auctionId = Integer.parseInt(request.getParameter("auction_id"));
        } catch (NumberFormatException | NullPointerException e){

            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Incorrect or missing param values");
            return;
        }

        // get Auction Data
        User user = (User) session.getAttribute("user");

        DashboardAuctionDAO dashboardAuctionDAO = new DashboardAuctionDAO(connection);
        DashboardAuction dashboardAuction = null;

        try {

            dashboardAuction = dashboardAuctionDAO.getAuctionDetail(auctionId);
        } catch (SQLException e){

            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Unable to recover Auction");
            return;
        }

        // auction validation
        if (dashboardAuction == null){

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Auction not found");
            return;
        }

        if (dashboardAuction.getId_user() != user.getId()){

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().println("Unauthorized user");
            return;
        }

        if (dashboardAuction.isClosed()){

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().println("Auction is already closed");
            return;
        }

        if (dashboardAuction.isOutDated()){

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().println("Auction has past end date");
            return;
        }

        // get offer list
        OfferDAO offerDAO = new OfferDAO(connection);
        List<Offer> offerList = null;

        try {

            offerList = offerDAO.offerListForAuction(dashboardAuction.getId());
        } catch (SQLException e){

            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Unable to recover Offer list");
            return;
        }

        // return a json of the list
        String json = new Gson().toJson(offerList);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }
}