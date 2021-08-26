package it.polimi.tiw.tiwprojectjs.controllers;

import it.polimi.tiw.tiwprojectjs.beans.DashboardAuction;
import it.polimi.tiw.tiwprojectjs.beans.Offer;
import it.polimi.tiw.tiwprojectjs.beans.User;
import it.polimi.tiw.tiwprojectjs.dao.DashboardAuctionDAO;
import it.polimi.tiw.tiwprojectjs.dao.OfferDAO;
import it.polimi.tiw.tiwprojectjs.utils.ConnectionHandler;
import org.apache.commons.lang3.StringEscapeUtils;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet(name = "AddOffer", value = "/AddOffer")
public class AddOffer extends HttpServlet {

    private Connection connection =null;

    public void init() throws ServletException{

        connection = ConnectionHandler.getConnection(getServletContext());
    }

    /**
     * params:
     * amount -> required
     * id_auction -> required
     * sh_address -> required, max 100
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession();
        if (session.isNew() || session.getAttribute("user") == null) {

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().println("unauthorized user");
            return;
        }

        // get parameter from request
        User currentUser = (User) session.getAttribute("user");
        float amount = -1;
        int id_auction = -1;
        String sh_address = null;

        try {

            amount = Float.parseFloat(request.getParameter("amount"));
            id_auction = Integer.parseInt(request.getParameter("id_auction"));
            sh_address = StringEscapeUtils.escapeJava(request.getParameter("sh_address"));
        } catch (NumberFormatException | NullPointerException e){

            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Incorrect or missing param values");
            return;
        }

        // validation
        if ((sh_address==null) || (sh_address.isEmpty()) || (sh_address.length() > 100)){

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Shipping Address is empty or longer than 100 char");
            return;
        }

        // get auction by specified id
        DashboardAuctionDAO dashboardAuctionDAO = new DashboardAuctionDAO(connection);
        DashboardAuction dashboardAuction = null;

        try {

            dashboardAuction = dashboardAuctionDAO.getAuctionDetail(id_auction);
        } catch ( SQLException e){

            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Not possible to retrieve specified Auction");
            return;
        }

        // validation
        if (dashboardAuction == null){

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Auction not found");
            return;
        }

        if (dashboardAuction.isClosed()){

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Auction is closed");
            return;
        }

        if ( (amount == -1) || (amount < (dashboardAuction.getMinOffer()))){

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Offer is less than required");
            return;
        }

        // create and save new Offer

        OfferDAO offerDAO = new OfferDAO(connection);
        Offer offer = new Offer(
                amount,
                sh_address,
                currentUser.getId(),
                id_auction

        );

        try {
            offerDAO.createOffer(offer);
        } catch (SQLException e){

            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Not possible to save Offer");
            return;
        }

        // send answer
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().println("Offer Added");
    }

    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
