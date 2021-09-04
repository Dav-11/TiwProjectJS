{

    function AlertBox(_messageContainer, _messageId){

        this.messageContainer = _messageContainer;
        this.messageId = _messageId;

        this.show = (message) => {
            this.messageContainer.style.display = "block";
            this.messageId.textContent = message;
        }

        this.reset = () => {
            this.messageContainer.style.display = "none";
        }
    }

    function Menu(_alertBox){

        this.alertBox = _alertBox;

        this.registerEvents = (orchestrator) => {

            document.getElementById("sell_button").addEventListener("click", () => {

                this.alertBox.reset();
                orchestrator.showSell();
            })

            document.getElementById("purchase_button").addEventListener("click", () => {

                setCookie("lastAction", "COMPRO", 30);
                this.alertBox.reset();
                orchestrator.showPurchase();
            })

            document.getElementById("logout_button").addEventListener("click", () => {

                this.alertBox.reset();
                let self = this;

                makeCall("POST", "Logout", null, (request) => {

                    if (request.readyState === XMLHttpRequest.DONE){

                        let message = request.responseText;

                        switch (request.status){

                            case 200:
                                cancelCookie('username');
                                window.location.href = "index.html";
                                break;

                            case 403:
                                cancelCookie('username');
                                window.location.href = "index.html";
                                break;

                            default:
                                self.alertBox.show(message);
                                break;
                        }
                    }
                })
            })
        }
    }

    function AuctionSearch(_alertBox, _formContainer){

        this.alertBox = _alertBox;
        this.formContainer = _formContainer;

        this.registerEvent = (orchestrator) => {

            this.formContainer.querySelector("input[type='button']").addEventListener('click', (e) => {

                this.alertBox.reset();
                let form = e.target.closest("form")

                if (form.checkValidity()){

                    let self = this;

                    makeCall("POST", 'SearchAuction', form, (request) => {

                        if (request.readyState === XMLHttpRequest.DONE) {

                            let message = request.responseText;

                            switch (request.status){

                                case 200:

                                    let auctionList = JSON.parse(request.responseText);
                                    purchaseAuctionList.show(auctionList);
                                    return;

                                case 403:
                                    cancelCookie('username');
                                    window.location.href = "index.html";
                                    break;

                                default:
                                    self.alertBox.show(message);
                                    break;
                            }

                        }
                    })

                } else {

                    form.reportValidity();
                }
            })
        }

        this.hide = () => {
            this.formContainer.style.display = "none";
        }

        this.show = () => {
            this.formContainer.style.display = "block";
        }
    }

    function PurchaseAuctionList(_alertBox, _listContainer, _listContainerBody){

        this.alertBox = _alertBox;
        this.listContainer = _listContainer;
        this.listContainerBody = _listContainerBody;

        this.hide = () => {
            this.listContainer.style.display = "none";
        }

        this.show = (auctionList) => {

            if (auctionList){

                this.listContainer.style.display = "block";
                let errorBox = document.getElementById("purchase_auction_list_message");
                errorBox.textContent = "";

                this.listContainerBody.innerHTML = ""; // clears the body

                if (auctionList.length === 0){
                    errorBox.textContent = "No mission found";
                    return;

                }
                let row, nameCell, descriptionCell, winningBetCell, daysLeftCell, hoursLeftCell, linkCell, anchor, linkText;
                let self = this;

                auctionList.forEach( (auction) => {

                    row = document.createElement("tr");

                    nameCell = document.createElement("td");
                    nameCell.textContent = auction.name;
                    row.appendChild(nameCell);

                    descriptionCell = document.createElement("td");
                    descriptionCell.textContent = auction.description;
                    row.appendChild(descriptionCell);

                    winningBetCell = document.createElement("td");
                    winningBetCell.textContent = auction.winningBet;
                    row.appendChild(winningBetCell);

                    daysLeftCell = document.createElement("td");
                    daysLeftCell.textContent = auction.daysRemaining;
                    row.appendChild(daysLeftCell);

                    hoursLeftCell = document.createElement("td");
                    hoursLeftCell.textContent = auction.hoursRemaining;
                    row.appendChild(hoursLeftCell);

                    linkCell = document.createElement("td");
                    anchor = document.createElement("a");
                    linkCell.appendChild(anchor);
                    linkText = document.createTextNode("Link");
                    anchor.appendChild(linkText);
                    anchor.setAttribute('auction_id', auction.id);

                    anchor.addEventListener("click", (e) =>{

                        self.alertBox.reset();

                        let auctionId = e.target.getAttribute("auction_id");

                        let jsonCookie = getCookie("recentlyViewedAuction");
                        if (jsonCookie) {

                            let recentlyViewedAuction = JSON.parse(jsonCookie);
                            if ( !(recentlyViewedAuction.includes(auctionId)) ){

                                recentlyViewedAuction.push(auctionId);
                            }

                            let newJson = JSON.stringify(recentlyViewedAuction);
                            setCookie("recentlyViewedAuction", newJson, 30);
                        } else {

                            let recentlyViewedAuction = new Array(auctionId);
                            let newJson = JSON.stringify(recentlyViewedAuction);
                            setCookie("recentlyViewedAuction", newJson, 30);
                        }
                        pageOrchestrator.showPurchaseAuction(auctionId);
                    });

                    anchor.href = "#";
                    row.appendChild(linkCell);

                    self.listContainerBody.appendChild(row);
                });
            }
        }

        this.initialShow = () => {

            let jsonCookie = getCookie("recentlyViewedAuction");
            let auctionList = new Array();

            if ( (! (jsonCookie) ) || (JSON.parse(jsonCookie).length === 0) ){
                this.show(auctionList);
                return;
            }

            if (jsonCookie) {

                let recentlyViewedAuction = JSON.parse(jsonCookie);
                let size = recentlyViewedAuction.length;

                let self = this;

                recentlyViewedAuction.forEach( (id) => {

                    makeCall("GET", "GetAuctionDetail?auction_id=" + id, null, (request) => {

                        if (request.readyState === XMLHttpRequest.DONE) {

                            let message = request.responseText;

                            switch (request.status) {

                                case 200:

                                    let auction = JSON.parse(request.responseText);

                                    if (auction.open){

                                        auctionList.push(auction);
                                    } else {

                                        size--;
                                    }

                                    if (auctionList.length === size){
                                        self.show(auctionList);
                                    }

                                    break;

                                case 403:
                                    cancelCookie('username');
                                    window.location.href = "index.html";
                                    break;

                                default:
                                    self.alertBox.show(message);
                                    return;
                            }
                        }
                    });
                })
            }
        }
    }

    function OpenAuctionList (_alertBox, _listContainer, _listContainerBody){

        this.alertBox = _alertBox;
        this.listContainer = _listContainer;
        this.listContainerBody = _listContainerBody;

        this.hide = () => {
            this.listContainer.style.display = "none";
        }

        this.show = () => {

            let self = this;
            let auctionList = null;

            this.listContainer.style.display = "block";
            let errorBox = document.getElementById("purchase_auction_list_message");
            errorBox.textContent = "";
            this.listContainerBody.innerHTML = "";

            makeCall("GET", "GetAuctionData?type=" + "OPEN", null, (request) => {

                if (request.readyState === XMLHttpRequest.DONE) {

                    let message = request.responseText;

                    switch (request.status) {

                        case 200:

                            auctionList = JSON.parse(request.responseText);

                            if (auctionList.length === 0){
                                errorBox.textContent = "No mission found";
                                return;

                            }
                            let row, codeCell, nameCell, winningBetCell, daysLeftCell, hoursLeftCell, linkCell, anchor, linkText;

                            auctionList.forEach( (auction) => {

                                row = document.createElement("tr");

                                codeCell = document.createElement("td");
                                codeCell.textContent = auction.id;
                                row.appendChild(codeCell);

                                nameCell = document.createElement("td");
                                nameCell.textContent = auction.name;
                                row.appendChild(nameCell);

                                winningBetCell = document.createElement("td");
                                winningBetCell.textContent = auction.winningBet;
                                row.appendChild(winningBetCell);

                                daysLeftCell = document.createElement("td");
                                daysLeftCell.textContent = auction.daysRemaining;
                                row.appendChild(daysLeftCell);

                                hoursLeftCell = document.createElement("td");
                                hoursLeftCell.textContent = auction.hoursRemaining;
                                row.appendChild(hoursLeftCell);

                                linkCell = document.createElement("td");
                                anchor = document.createElement("a");
                                linkCell.appendChild(anchor);
                                linkText = document.createTextNode("Link");
                                anchor.appendChild(linkText);
                                anchor.setAttribute('auction_id', auction.id);

                                anchor.addEventListener("click", (e) =>{

                                    self.alertBox.reset();
                                    let auctionId = e.target.getAttribute("auction_id");

                                    if ( (auction.daysRemaining === 0) && (auction.hoursRemaining === 0) ){

                                        pageOrchestrator.showOutDatedAuction(auctionId);
                                    } else {

                                        pageOrchestrator.showOpenAuction(auctionId);
                                    }
                                });

                                anchor.href = "#";
                                row.appendChild(linkCell);

                                self.listContainerBody.appendChild(row);
                            });
                            break;

                        case 403:
                            cancelCookie('username');
                            window.location.href = "index.html";
                            break;

                        default:

                            errorBox.textContent = "ERROR: " + message;
                            return;
                    }
                }
            })
        }
    }

    function ClosedAuctionList(_alertBox, _listContainer, _listContainerBody){

        this.alertBox = _alertBox;
        this.listContainer = _listContainer;
        this.listContainerBody = _listContainerBody;

        this.hide = () => {
            this.listContainer.style.display = "none";
        }

        this.show = () => {

            let self = this;
            let auctionList = null;

            this.listContainer.style.display = "block";
            let errorBox = document.getElementById("purchase_auction_list_message");
            errorBox.textContent = "";
            this.listContainerBody.innerHTML = "";

            makeCall("GET", "GetAuctionData?type=" + "CLOSED", null, (request) => {

                if (request.readyState === XMLHttpRequest.DONE) {

                    let message = request.responseText;

                    switch (request.status) {

                        case 200:
                            auctionList = JSON.parse(request.responseText);

                            if (auctionList.length === 0){

                                errorBox.textContent = "No mission found";
                                return;
                            }

                            let row, codeCell, nameCell, winningBetCell, linkCell, anchor, linkText;

                            auctionList.forEach( (auction) => {

                                row = document.createElement("tr");

                                codeCell = document.createElement("td");
                                codeCell.textContent = auction.id;
                                row.appendChild(codeCell);

                                nameCell = document.createElement("td");
                                nameCell.textContent = auction.name;
                                row.appendChild(nameCell);

                                winningBetCell = document.createElement("td");
                                winningBetCell.textContent = auction.winningBet;
                                row.appendChild(winningBetCell);

                                linkCell = document.createElement("td");
                                anchor = document.createElement("a");
                                linkCell.appendChild(anchor);
                                linkText = document.createTextNode("Link");
                                anchor.appendChild(linkText);
                                anchor.setAttribute('auction_id', auction.id);

                                anchor.addEventListener("click", (e) =>{

                                    self.alertBox.reset();
                                    let auctionId = e.target.getAttribute("auction_id");
                                    pageOrchestrator.showClosedAuction(auctionId);
                                });

                                anchor.href = "#";
                                row.appendChild(linkCell);

                                self.listContainerBody.appendChild(row);
                            });
                            break;

                        case 403:
                            cancelCookie('username');
                            window.location.href = "index.html";
                            break;

                        default:
                            errorBox.textContent = "ERROR: " + message;
                            return;
                    }
                }
            });
        }
    }

    function AuctionCreateForm(_alertBox, _formContainer) {

        this.formContainer =_formContainer;

        this.hide = () => {

            this.formContainer.style.display = "none";
        }

        this.show =() => {

            this.formContainer.style.display = "block";
        }

        this.registerEvent = (orchestrator) => {

            this.formContainer.querySelector("input[type='button']").addEventListener('click', (e) => {


                let form = e.target.closest("form")

                if (form.checkValidity()){

                    let self = this;

                    makeCall("POST", "CreateAuction", form, (request) => {

                        if (request.readyState === XMLHttpRequest.DONE){

                            let message = request.responseText;

                            switch (request.status){

                                case 200:

                                    setCookie("lastAction", "VENDO", 30);
                                    openAuctionList.show();
                                    closedAuctionList.show();
                                    break;

                                case 403:
                                    cancelCookie('username');
                                    window.location.href = "index.html";
                                    break;

                                default:

                                    alert(message);
                                    break;
                            }
                        }
                    });
                } else {

                    form.reportValidity();
                }
            });
        }
    }

    function AuctionWinnerDetail(_divContainer){

        this.divContainer = _divContainer;

        this.hide = () => {
            this.divContainer.style.display = "none";
        }

        this.show = (auctionId) => {

            this.divContainer.style.display = "block";

            makeCall("GET", "GetOfferData?auction_id=" + auctionId + "&type=" + "WINNING", null, (request) => {

                if (request.readyState === XMLHttpRequest.DONE) {

                    let message = request.responseText;

                    switch (request.status) {

                        case 200:

                            let offer = JSON.parse(request.responseText);

                            document.getElementById("winner_name").textContent = offer.userUserName;
                            document.getElementById("winner_bet").textContent = offer.amount;
                            document.getElementById("winner_sh_address").textContent = offer.sh_address;
                            break;

                        case 403:
                            cancelCookie('username');
                            window.location.href = "index.html";
                            break;

                        default:
                            document.getElementById("auction_winner_detail_message").textContent = message;
                            break;
                    }
                }
            });
        }
    }

    function OfferForm(_divContainer){

        this.divContainer = _divContainer;

        this.hide = () => {
            this.divContainer.style.display = "none";
        }

        this.show = (auctionId) => {

            let auction = null;
            let minOffer = null;

            makeCall("GET", "GetAuctionDetail?auction_id=" + auctionId, null, (request) => {

                if (request.readyState === XMLHttpRequest.DONE) {

                    let message = request.responseText;

                    switch (request.status) {

                        case 200:

                            // get the auction
                            auction = JSON.parse(request.responseText);

                            // calculate the min offer
                            if (auction.winningBet > 0){
                                minOffer = auction.winningBet + auction.min_rise;
                            } else {
                                minOffer = auction.initial_price + auction.min_rise;
                            }

                            // sets the min offer
                            document.getElementById("minimum_bet").textContent = minOffer;

                            // sets the auction_id as a hidden input
                            this.divContainer.querySelector("input[type = 'hidden']").value = auction.id;

                            break;

                        case 403:
                            cancelCookie('username');
                            window.location.href = "index.html";
                            break;

                        default:
                            document.getElementById("purchase_offer_message").textContent = message;
                            return;
                    }
                }
            });

            // shows the div
            this.divContainer.style.display = "block";
        }

        this.registerEvents = () =>{

            this.divContainer.querySelector("input[type='button']").addEventListener('click', (e) => {

                // gets the form
                let form = e.target.closest("form");

                if (form.checkValidity()){

                    // check if the specified amount is more then the minimum
                    let amount = this.divContainer.querySelector('[name="amount"]').value;

                    // get the minOffer attribute
                    let minOffer = document.getElementById("minimum_bet").textContent;

                    // checks if the offer is less than the minimum
                    if (amount < minOffer){

                        alert("L'offerta deve essere maggiore del minimo di: " + minOffer);
                        return;
                    } else {

                        // makes the call to save the offer
                        makeCall("POST", "AddOffer", form, (request)=>{

                            if (request.readyState === XMLHttpRequest.DONE) {

                                let message = request.responseText;

                                switch (request.status) {

                                    case 200:
                                        pageOrchestrator.showPurchase();
                                        break;

                                    case 403:
                                        cancelCookie('username');
                                        window.location.href = "index.html";
                                        break;

                                    default:
                                        alert(message);
                                        break;
                                }
                            }
                        })
                    }
                } else {

                    form.reportValidity();
                }
            });
        }
    }

    function AuctionCloseButton(_divContainer){

        this.divContainer = _divContainer;

        this.hide = () => {
            this.divContainer.style.display = "none";
        }

        this.registerEvents = () => {

            this.divContainer.querySelector("input[type='button']").addEventListener('click', (e) => {

                let form = e.target.closest("form");

                if (form.checkValidity()){

                    makeCall("POST", "CloseAuction", form, (request) => {

                        if (request.readyState === XMLHttpRequest.DONE) {

                            let message = request.responseText;

                            switch (request.status) {

                                case 200:
                                    openAuctionList.show();
                                    closedAuctionList.show();
                                    pageOrchestrator.hideRightPart();
                                    break;

                                case 403:
                                    cancelCookie('username');
                                    window.location.href = "index.html";
                                    break;

                                default:
                                    alert(message);
                                    break;

                            }
                        }
                    })
                } else {
                    form.reportValidity();
                }
            })
        }

        this.show = (auctionId) => {

            this.divContainer.querySelector("input[type = 'hidden']").value = auctionId;

            this.divContainer.style.display = "block";
        }
    }

    function AuctionOfferList(_divContainer,_bodyContainer){

        this.divContainer = _divContainer;
        this.bodyContainer = _bodyContainer;

        this.hide = () => {
            this.divContainer.style.display = "none";
        }

        this.show = (auctionId) => {

            let self = this;
            this.divContainer.style.display = "block";

            let errorBox = document.getElementById("auction_offer_list_message");
            errorBox.textContent = "";
            this.bodyContainer.innerHTML = "";

            makeCall("GET", "GetOfferData?auction_id=" + auctionId + "&type=" + "LIST", null, (request) => {

                if (request.readyState === XMLHttpRequest.DONE) {

                    let message = request.responseText;

                    switch (request.status) {

                        case 200:

                            let offerList = JSON.parse(request.responseText);

                            if (offerList.length === 0){
                                errorBox.textContent = "No offer found";
                                return;
                            }

                            let row, offerCell, userCell, dateCell

                            offerList.forEach( (offer) => {

                                row = document.createElement("tr");

                                offerCell = document.createElement("td");
                                offerCell.textContent = offer.amount;
                                row.appendChild(offerCell);

                                userCell = document.createElement("td");
                                userCell.textContent = offer.userUserName;
                                row.appendChild(userCell);

                                dateCell = document.createElement("td");
                                dateCell.textContent = offer.date;
                                row.appendChild(dateCell);

                                self.bodyContainer.appendChild(row);
                            })
                            break;

                        case 403:
                            cancelCookie('username');
                            window.location.href = "index.html";
                            break;

                        default:
                            errorBox.textContent = "ERROR: " + message;
                            return;
                    }
                }
            });
        }
    }

    function AuctionDetail(_divContainer){

        this.divContainer = _divContainer;

        this.hide = () => {
            this.divContainer.style.display = "none";
        }

        this.show = (auctionId) => {

            this.divContainer.style.display = "block";

            let oldDescription = document.getElementById("auction_detail_description");
            if (oldDescription){
                oldDescription.parentNode.removeChild(oldDescription);
            }

            let oldImage = document.getElementById("auction_detail_image");
            if (oldImage){
                oldImage.parentNode.removeChild(oldImage);
            }

            makeCall("GET", "GetAuctionDetail?auction_id=" + auctionId, null, (request) => {

                if (request.readyState === XMLHttpRequest.DONE) {

                    let message = request.responseText;

                    switch (request.status){

                        case 200:
                            let auction = JSON.parse(request.responseText);

                            document.getElementById("detail_start_date").textContent = auction.start_date;
                            document.getElementById("detail_end_date").textContent = auction.end_date;
                            document.getElementById("detail_initial_price").textContent = auction.initial_price;
                            document.getElementById("detail_min_rise").textContent = auction.min_rise;
                            document.getElementById("detail_item_name").textContent = auction.name;
                            document.getElementById("detail_item_id").textContent = auction.item_id;

                            if (auction.description){

                                let descriptionParagraph = document.createElement("p");
                                descriptionParagraph.id = "auction_detail_description";
                                descriptionParagraph.textContent = "Descrizione: " + auction.description;
                                this.divContainer.appendChild(descriptionParagraph);
                            }

                            if (auction.picture){

                                let image = document.createElement("img");
                                image.id = "auction_detail_image";
                                image.src = 'data:image/jpeg;base64,' + auction.picture;
                                this.divContainer.appendChild(image);
                            }
                            break;

                        case 403:
                            cancelCookie('username');
                            window.location.href = "index.html";
                            break;

                        default:
                            let errorParagraph = document.createElement("p");
                            errorParagraph.textContent = "ERROR: " + message;
                            return;
                    }
                }
            });
        }
    }

    function PageOrchestrator(){

        this.start = () => {

            alertBox = new AlertBox(
                document.getElementById("alert_box"),
                document.getElementById("alert_message")
            );
            alertBox.reset();

            menu = new Menu(alertBox);
            menu.registerEvents(this);

            auctionSearch = new AuctionSearch(
                alertBox,
                document.getElementById("purchase_auction_search")
            );
            auctionSearch.registerEvent(this);
            auctionSearch.hide();

            purchaseAuctionList = new PurchaseAuctionList(
                alertBox,
                document.getElementById("purchase_auction_list"),
                document.getElementById("purchase_auction_list_body")
            );
            purchaseAuctionList.hide();

            openAuctionList = new OpenAuctionList(
                alertBox,
                document.getElementById("open_auction_list"),
                document.getElementById("open_auction_list_body")
            );
            openAuctionList.hide();

            closedAuctionList = new ClosedAuctionList(
                alertBox,
                document.getElementById("closed_auction_list"),
                document.getElementById("closed_auction_list_body")
            );
            closedAuctionList.hide();

            auctionCreateForm = new AuctionCreateForm(
                alertBox,
                document.getElementById("auction_create_form")
            );
            auctionCreateForm.registerEvent(this);
            auctionCreateForm.hide();

            auctionWinnerDetail = new AuctionWinnerDetail(
                document.getElementById("auction_winner_detail")
            );
            auctionWinnerDetail.hide();

            offerForm = new OfferForm(
                document.getElementById("purchase_offer")
            );
            offerForm.registerEvents();
            offerForm.hide();

            auctionCloseButton = new AuctionCloseButton(
                document.getElementById("auction_close_button")
            );
            auctionCloseButton.registerEvents();
            auctionCloseButton.hide();

            auctionOfferList = new AuctionOfferList(
                document.getElementById("auction_offer_list"),
                document.getElementById("offer_list_body")
            );
            auctionOfferList.hide();

            auctionDetail = new AuctionDetail(
                document.getElementById("auction_detail")
            )
            auctionDetail.hide();

            let lastAction = getCookie("lastAction");
            if (lastAction === "VENDO"){

                this.showSell();
            } else {

                this.showPurchase();
            }

        }

        this.showPurchase = () => {

            auctionSearch.show();
            purchaseAuctionList.initialShow()

            openAuctionList.hide();
            closedAuctionList.hide();
            auctionCreateForm.hide();

            this.hideRightPart();
        }

        this.showSell = () => {

            auctionSearch.hide();
            purchaseAuctionList.hide();

            openAuctionList.show();
            closedAuctionList.show();
            auctionCreateForm.show();

            this.hideRightPart();
        }

        this.hideRightPart = () => {
            alertBox.reset();
            auctionWinnerDetail.hide();
            offerForm.hide();
            auctionCloseButton.hide();
            auctionOfferList.hide();
            auctionDetail.hide();

        }

        this.showOpenAuction = (auctionId) => {
            this.hideRightPart();

            auctionOfferList.show(auctionId);
            auctionDetail.show(auctionId);
        }

        this.showOutDatedAuction = (auctionId) => {
            this.hideRightPart();

            auctionCloseButton.show(auctionId);
            auctionOfferList.show(auctionId);
            auctionDetail.show(auctionId);
        }

        this.showClosedAuction = (auctionId) => {
            this.hideRightPart();

            auctionWinnerDetail.show(auctionId);
            auctionDetail.show(auctionId);
        }

        this.showPurchaseAuction = (auctionId) => {
            this.hideRightPart();

            auctionDetail.show(auctionId);
            offerForm.show(auctionId);
        }
    }


    // main
    let alertBox, menu, auctionSearch, purchaseAuctionList, openAuctionList, closedAuctionList, auctionCreateForm, auctionWinnerDetail, offerForm, auctionCloseButton, auctionOfferList, auctionDetail ;
    let pageOrchestrator = new PageOrchestrator();

    window.addEventListener("load", () => {

        const userName = getCookie('username');
        const lastUser = getCookie('lastUser');

        if ( !(userName) ){
            window.location.href = "index.html";
        } else {

            if ( (!(lastUser)) || (lastUser !== userName) ){

                cancelCookie('recentlyViewedAuction');
                cancelCookie("lastAction");
            }

            setCookie('lastUser', userName, 30);

            pageOrchestrator.start();
        }
    }, false);
}
