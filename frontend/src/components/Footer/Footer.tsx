import "./Footer.css";

function Footer(){
    return (
        <footer className="footer">
            <div className="footer-left">
                <a href="https://docs.sellhelp.org" target="_blank" rel="noopener noreferrer">
                    Dokumentáció
                </a>
            </div>

            <div className="footer-center">
                <img src="/images/favicon-white.png" alt="logo" />
            </div>

            <div className="footer-right">
                <p>&copy; Minden jog fenttartva</p>
            </div>
        </footer>
    )
}

export default Footer;