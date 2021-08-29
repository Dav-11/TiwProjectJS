package it.polimi.tiw.tiwprojectjs.controllers;

import it.polimi.tiw.tiwprojectjs.beans.DashboardAuction;
import it.polimi.tiw.tiwprojectjs.beans.User;
import it.polimi.tiw.tiwprojectjs.dao.AuctionDAO;
import it.polimi.tiw.tiwprojectjs.dao.DashboardAuctionDAO;
import it.polimi.tiw.tiwprojectjs.utils.ConnectionHandler;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

@WebServlet(name = "CloseAuction", value = "/CloseAuction")
@MultipartConfig
public class CloseAuction extends HttpServlet {

    private Connection connection = null;

    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    /**
     * params:
     * auction_id -> required
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

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

        // Get the specified Auction, performs check and close it
        User user = (User) session.getAttribute("user");

        DashboardAuctionDAO dashboardAuctionDAO = new DashboardAuctionDAO(connection);
        DashboardAuction dashboardAuction = null;

        AuctionDAO auctionDAO = new AuctionDAO(connection);

        try {

            dashboardAuction = dashboardAuctionDAO.getAuctionDetail(auctionId);

            if (dashboardAuction == null){

                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("Auction not found");
                return;
            }

            if (dashboardAuction.getId_user() != user.getId()){

                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("User is not the owner");
                return;
            }

            if (dashboardAuction.isClosed()){

                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("Auction is already closed");
                return;
            }

            if (!dashboardAuction.isOutDated()){

                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("Auction has not past end date yet");
                return;
            }

            auctionDAO.closeAuction(auctionId);

        } catch (SQLException e){

            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Unable to recover Auction");
            return;
        }

        // send answer
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().println("Auction Closed");
    }

    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
