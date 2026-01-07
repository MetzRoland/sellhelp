import { Link } from 'react-router';
import './Header.css';

function Header() {
    return (
        <header className="header">
            <nav className="header-nav">
                <div className="left-options">
                    <Link className='nav-link' to="#">Posztok keresése</Link>
                </div>
                <div className="title-option">
                    <Link className='nav-link main-page-link' to="#">SellHelp</Link>
                </div>
                <div className="right-options">
                    <Link className='nav-link' to="#">Bejelentkezés</Link>
                    <Link className='nav-link' to="#">Regiszrálás</Link>
                </div>
            </nav>
        </header>
    );
}

export default Header;