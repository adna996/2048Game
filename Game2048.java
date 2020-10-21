package projekat_2048;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;
 
public class Game2048 extends JPanel {
	
	/**
	 * Na samom pocetku kreiramo enum pod nazivom State. U kojem 
	 * cuvamo sve ono sto nam je potrebno za dalji rad. Stanja koja 
	 * cuvamo su start, pobjeda, itd. Enum je skupina nepromjenljivih 
	 * varijabli tj predstavlja grupu "konstanti". Zasto koristimo enum
	 * lezi u cinjenici da je to laksi nacin neo da svaki put iz pocetka 
	 * kreiramo. 
	*/
	
    enum State {
        start, won, running, over
    }
    /**
	 * Zatim, kreiramo tj incijaliziramo niz koji ce cuvati boje kocki polja. 
	 * S obzirom da za svaki broj imamo drugu boju potrebno je boje cuvati, 
	 * a najlaksi nacin cuvanja boja jeste preko niza. Dakle, sa ovim nizom 
	 * kreiramo niz varijabli koje ustvari referenciraju na neki objekat, i te
	 * varijable ne mogu biti promjenjene da pokazuju na bilo sta drugo, 
	 * ali clanovi niza zato mogu biti mijenjanji. Boje niza su odredjene hex 
	 * vrjednostima boja. Pri tome je bitno napomenuti da je Color dio Java awt 
	 * paketa. 
	 */
    final Color[] colorTable = {
        new Color(0x701710), new Color(0xFFE4C3), new Color(0xfff4d3),
        new Color(0xffdac3), new Color(0xe7b08e), new Color(0xe7bf8e),
        new Color(0xffc4c3), new Color(0xE7948e), new Color(0xbe7e56),
        new Color(0xbe5e56), new Color(0x9c3931), new Color(0x701710)};
    /**
    *Nakon toga incijalitiramo staticku cijelobrojnu varijablu cilj i 
    *njenu vrijednost postavimo na 2048.  
    */
    
    final static int target = 2048;
 
    static int highest;
    static int score;
    

    /**
     * Kreiramo i privatne varijable za postavljanje boja prozora.
     */
    private Color gridColor = new Color(0xBBADA0);
    private Color emptyColor = new Color(0xCDC1B4);
    private Color startColor = new Color(0xFFEBCD);
 
    private Random rand = new Random();
 
    private Tile[][] tiles;
    private int side = 4;
    private State gamestate = State.start;
    private boolean checkingAvailableMoves;
    /**
     * Kreiramo konstrukor koji koristimo za pozivanje igrice.
     * Postavljamo dimenzije prozora, boju porozra te font slova. 
     * Takodjer dodajemo i MouseListener koji treba da se aktivira na 
     * startGame() i repaint() funkcije. 
     * Pored toga imamo i KeyListener koji ce pratiti tipke koje su 
     * pristinute tj gore, dolje, lijevo ili desno i za svaki od tih pokreta
     * ce pozivati kreirane funkcije tj funkcije pomjeriGore, pomjeriDolje itd.
     * Dakle, svaki put kad je pritisnut, poslat ce se KeyCode i testirat ce se 
     * neki od uslova. Ako je uslov ispunje onda ce se pokretati gore pomenute 
     * funkcije.
     * Svaki put, kad se pomjere polja mi ustvari moramo da ih repaintamo pa 
     * zbog toga koristimo funkciju ugradjeno funkciju repaint() koja to radi
     * za nas. Funkciju koristimo i za svaki klik misa i pritisak dugmeta. 
     * S ovim smo zavrsili kreiranje konstruktora. 
     */
    public Game2048() {
        setPreferredSize(new Dimension(900, 700));
        setBackground(new Color(0xFAF8EF));
        setFont(new Font("SansSerif", Font.BOLD, 48));
        setFocusable(true);
 
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                startGame();
                repaint();
            }
        });
 
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        moveUp();
                        break;
                    case KeyEvent.VK_DOWN:
                        moveDown();
                        break;
                    case KeyEvent.VK_LEFT:
                        moveLeft();
                        break;
                    case KeyEvent.VK_RIGHT:
                        moveRight();
                        break;
                }
                repaint();
            }
        });
    }
    
    /**
     * Sada kreiramo funkciju koja radi je sljedece. 
     * Kao parametar prima tipa Graphics. S obzirom da necemo da obojimo cijelu 
     * komponentu to koristimo super.paintComponent funkciju koja ce osigurati da
     * smo obojili samo to sto nam treba. Koristenje Graphics2D nam omogucava sofisticiraniju 
     * kontrolu nad objektima, transformacije koordinata, upravljanje bojama i izgled teksta. 
     * Ova klasa renderuje 2D oblike, teksti i slike. Zato i incijaliziramo.Sa RenderingHints mi 
     * ustvari odjredjujemo gdje zelimo renderovati objekat sto je brze moguce. Sa setRenderingHint
     * mi objekat "prebacujemo" u Graphics2D. Na kraju pozivamo i funkciju nacrtajResetku(g) koja crta 
     * resetku. Dakle koristimo je crtanje i postavljanje.
     */
    @Override
    public void paintComponent(Graphics gg) {
        super.paintComponent(gg);
        Graphics2D g = (Graphics2D) gg;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
 
        drawGrid(g);
    }
    
    
     /**
      * Potrebno je kreirati funkciju za pocinjanje igrice. Dakle ukoliko igrica nije u stanju igranja
      * tj ukoliko nije u stanju running score je na 0, ujedno sa njim i najveci skore je na 0.
      * Stanje igranja prebacujemo da je igra pocela tj u stanje running. 
      */
    void startGame() {
        if (gamestate != State.running) {
            score = 0;
            highest = 0;
            gamestate = State.running;
            tiles = new Tile[side][side];
            addRandomTile();
            addRandomTile();
        }
    }
    
    
    /**
     * Ovom funkcijom cratmo kvarate unutar kojih ce se igra odvijati. 
     * Ukoliko je igra u stanju running tj u stanju igranja nacrtat ce "resetka"
     * sa poljima. Unutar ove funkcije uredjujemo i izgled. Ukoliko igra nije u 
     * stanju igranja onda crtamo pocetni menu sa opcijom klikanja na pocetak igre, 
     * kao i opis onoga sto igra radi, tj kako se igra. Takodjer tu i definisemo 
     * sta ce desiti u stanju over i won.  
     * @param g
     */
    void drawGrid(Graphics2D g) {
        g.setColor(gridColor);
        g.fillRoundRect(200, 100, 499, 499, 15, 15);
 
        if (gamestate == State.running) {
 
            for (int r = 0; r < side; r++) {
                for (int c = 0; c < side; c++) {
                    if (tiles[r][c] == null) {
                        g.setColor(emptyColor);
                        g.fillRoundRect(215 + c * 121, 115 + r * 121, 106, 106, 7, 7);
                    } else {
                        drawTile(g, r, c);
                    }
                }
            }
        } else {
            g.setColor(startColor);
            g.fillRoundRect(215, 115, 469, 469, 7, 7);
 
            g.setColor(gridColor.darker());
            g.setFont(new Font("SansSerif", Font.BOLD, 128));
            g.drawString("2048", 310, 270);
 
            g.setFont(new Font("SansSerif", Font.BOLD, 20));
 
            if (gamestate == State.won) {
                g.drawString("Uspjeh!", 390, 350);
 
            } else if (gamestate == State.over)
                g.drawString("Game over :( ", 400, 350);
 
            g.setColor(gridColor);
            g.drawString("Pritisni za pocetak igrice!", 330, 470);
            g.drawString("(za pomjeranje koristi strelice)", 310, 530);
        }
    }
    
    void drawTile(Graphics2D g, int r, int c) {
        int value = tiles[r][c].getValue();
 
        g.setColor(colorTable[(int) (Math.log(value) / Math.log(2)) + 1]);
        g.fillRoundRect(215 + c * 121, 115 + r * 121, 106, 106, 7, 7);
        String s = String.valueOf(value);
 
        g.setColor(value < 128 ? colorTable[0] : colorTable[1]);
 
        FontMetrics fm = g.getFontMetrics();
        int asc = fm.getAscent();
        int dec = fm.getDescent();
 
        int x = 215 + c * 121 + (106 - fm.stringWidth(s)) / 2;
        int y = 115 + r * 121 + (asc + (106 - (asc + dec)) / 2);
 
        g.drawString(s, x, y);
    }
 
    /**
     * Ovom funkcijom postavljamo random polje i njegovu vrijednost. 
     */
    private void addRandomTile() {
        int pos = rand.nextInt(side * side);
        int row, col;
        do {
            pos = (pos + 1) % (side * side);
            row = pos / side;
            col = pos % side;
        } while (tiles[row][col] != null);
 
        int val = rand.nextInt(10) == 0 ? 4 : 2;
        tiles[row][col] = new Tile(val);
    }
 
    /**
     * Funkcijom move provjeravamo poteze
     * @param countDownFrom
     * @param yIncr
     * @param xIncr
     * @return
     */
    private boolean move(int countDownFrom, int yIncr, int xIncr) {
        boolean moved = false;
 
        for (int i = 0; i < side * side; i++) {
            int j = Math.abs(countDownFrom - i);
 
            int r = j / side;
            int c = j % side;
 
            if (tiles[r][c] == null)
                continue;
 
            int nextR = r + yIncr;
            int nextC = c + xIncr;
 
            while (nextR >= 0 && nextR < side && nextC >= 0 && nextC < side) {
 
                Tile next = tiles[nextR][nextC];
                Tile curr = tiles[r][c];
 
                if (next == null) {
 
                    if (checkingAvailableMoves)
                        return true;
 
                    tiles[nextR][nextC] = curr;
                    tiles[r][c] = null;
                    r = nextR;
                    c = nextC;
                    nextR += yIncr;
                    nextC += xIncr;
                    moved = true;
 
                } else if (next.canMergeWith(curr)) {
 
                    if (checkingAvailableMoves)
                        return true;
 
                    int value = next.mergeWith(curr);
                    if (value > highest)
                        highest = value;
                    score += value;
                    tiles[r][c] = null;
                    moved = true;
                    break;
                } else
                    break;
            }
        }
 
        if (moved) {
            if (highest < target) {
                clearMerged();
                addRandomTile();
                if (!movesAvailable()) {
                    gamestate = State.over;
                }
            } else if (highest == target)
                gamestate = State.won;
        }
 
        return moved;
    }
    
    /**
     * moveUp funkcijom pomjeramo se prema gore.
     * @return
     */
    
    boolean moveUp() {
        return move(0, -1, 0);
    }
    
    /**
     * funkcijom moveDown() pomjeramo se prema dolje.
     * @return
     */
    boolean moveDown() {
        return move(side * side - 1, 1, 0);
    }
    /**
     * moveleft() funckijom pomjeramo se na lijevo.
     * @return
     */
    boolean moveLeft() {
        return move(0, 0, -1);
    }
    /**
     * moveRight() funkcijom se pomjeramo na desno
     * @return
     */
    boolean moveRight() {
        return move(side * side - 1, 0, 1);
    }
 
    void clearMerged() {
        for (Tile[] row : tiles)
            for (Tile tile : row)
                if (tile != null)
                    tile.setMerged(false);
    }
    /**
     * funkcijom movesAvailable() provjeravamo dopstupnost tj korektnost poteza. Ovim 
     * se osiguravamo da je jedan potez dozvoljen u jednoj partiji. 
     * @return
     */
    boolean movesAvailable() {
        checkingAvailableMoves = true;
        boolean hasMoves = moveUp() || moveDown() || moveLeft() || moveRight();
        checkingAvailableMoves = false;
        return hasMoves;
    }
    
    /**
     * postavljamo JFrame() 
     * @param args
     */
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame();
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setTitle("2048");
            f.setResizable(true);
            f.add(new Game2048(), BorderLayout.CENTER);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}
 
/**
 * Kreiramo klasu Tile kojom se koristimo za za sve ostala podesavanja unutar 
 * klase Game2048. 
 * @author lenovo
 *
 */
class Tile {
    private boolean merged;
    private int value;
 
    Tile(int val) {
        value = val;
    }
 
    int getValue() {
        return value;
    }
 
    void setMerged(boolean m) {
        merged = m;
    }
 
    boolean canMergeWith(Tile other) {
        return !merged && other != null && !other.merged && value == other.getValue();
    }
 
    int mergeWith(Tile other) {
        if (canMergeWith(other)) {
            value *= 2;
            merged = true;
            return value;
        }
        return -1;
    }
}