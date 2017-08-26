package tc.oc.commons.bukkit.gui.interfaces.render.text;

import tc.oc.commons.bukkit.gui.Interface;
import tc.oc.commons.bukkit.gui.buttons.Button;
import tc.oc.commons.bukkit.gui.interfaces.render.ChestRenderInterface;
import tc.oc.commons.bukkit.gui.interfaces.render.Coordinate;
import tc.oc.commons.bukkit.util.ItemCreator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;


public class GridTextRenderInterface extends ChestRenderInterface {

    private String text;

    public GridTextRenderInterface(Player player, List<Button> buttons, int size, String title, String text) {
        super(player, buttons, size, title);
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    @Override
    public void updateButtons() {
        List<Button> buttons = new ArrayList<>();
        List<Letter> letters = new ArrayList<>();
        for (char c : getText().toCharArray()) {
            letters.add(Letter.getLetter(c));
        }
        double totalX = 0;
        for (Letter letter : letters) {
            for (final Coordinate coordinate : letter.getCoordinates().keySet()) {
                Coordinate newCoordinate = new Coordinate(coordinate.getX(), coordinate.getY());
                newCoordinate.setX(coordinate.getX() + totalX);
                int renderSlot = getRenderSlot(newCoordinate);
                if (renderSlot >= 0) {
                    ItemCreator itemCreator = letter.getCoordinates().get(coordinate) != null ? letter.getCoordinates().get(coordinate) : new ItemCreator(Material.WOOL).setData(6);
                    Button button = new Button(itemCreator.setName(newCoordinate.getX() + " " + newCoordinate.getY()), renderSlot);
                    buttons.add(button);
                }
            }
            totalX = totalX + letter.getLength() + 1;
        }

        Button up = new Button(new ItemCreator(Material.ARROW)
                .setName(ChatColor.GREEN + "Move Up"), 7) {
            @Override
            public void function(Player player) {
                setOrigin(new Coordinate(getOrigin().getX(), getOrigin().getY() + 1));
                updateButtons();
            }
        };
        buttons.add(up);
        Button down = new Button(new ItemCreator(Material.ARROW)
                .setName(ChatColor.GREEN + "Move Down"), 25) {
            @Override
            public void function(Player player) {
                setOrigin(new Coordinate(getOrigin().getX(), getOrigin().getY() - 1));
                updateButtons();
            }
        };
        buttons.add(down);
        Button left = new Button(new ItemCreator(Material.ARROW)
                .setName(ChatColor.GREEN + "Move Left"), 15) {
            @Override
            public void function(Player player) {
                setOrigin(new Coordinate(getOrigin().getX() - 1, getOrigin().getY()));
                updateButtons();
            }
        };
        buttons.add(left);
        Button right = new Button(new ItemCreator(Material.ARROW)
                .setName(ChatColor.GREEN + "Move Right"), 17) {
            @Override
            public void function(Player player) {
                setOrigin(new Coordinate(getOrigin().getX() + 1, getOrigin().getY()));
                updateButtons();
            }
        };
        buttons.add(right);
        Button compass = new Button(new ItemCreator(Material.COMPASS)
                .setName(ChatColor.GREEN + "Use these to move!"), 16);
        buttons.add(compass);
        setButtons(buttons);
        updateInventory();
    }

    public enum Letter {

        A(
                'A',
                Arrays.asList(
                        "_X_",
                        "X_X",
                        "X_X",
                        "XXX",
                        "X_X")),
        B(
                'B',
                Arrays.asList(
                        "XX_",
                        "X_X",
                        "XX_",
                        "X_X",
                        "XX_")),
        C(
                'C',
                Arrays.asList(
                        "_XX",
                        "X__",
                        "X__",
                        "X__",
                        "_XX")),
        D(
                'D',
                Arrays.asList(
                        "XX_",
                        "X_X",
                        "X_X",
                        "X_X",
                        "XX_")),
        E(
                'E',
                Arrays.asList(
                        "XXX",
                        "X__",
                        "XX_",
                        "X__",
                        "XXX")),
        F(
                'F',
                Arrays.asList(
                        "XXX",
                        "X__",
                        "XX_",
                        "X__",
                        "X__")),
        G(
                'G',
                Arrays.asList(
                        "XXX",
                        "X__",
                        "X__",
                        "X_X",
                        "XXX")),
        H(
                'H',
                Arrays.asList(
                        "X_X",
                        "X_X",
                        "XXX",
                        "X_X",
                        "X_X")),
        I(
                'I',
                Arrays.asList(
                        "XXX",
                        "_X_",
                        "_X_",
                        "_X_",
                        "XXX")),
        J(
                'J',
                Arrays.asList(
                        "__X",
                        "__X",
                        "__X",
                        "X_X",
                        "XXX")),
        K(
                'K',
                Arrays.asList(
                        "X_X",
                        "X_X",
                        "XX_",
                        "X_X",
                        "X_X")),
        L(
                'L',
                Arrays.asList(
                        "X__",
                        "X__",
                        "X__",
                        "X__",
                        "XXX")),
        M(
                'M',
                Arrays.asList(
                        "X_X",
                        "XXX",
                        "X_X",
                        "X_X",
                        "X_X")),
        N(
                'N',
                Arrays.asList(
                        "X__X",
                        "XX_X",
                        "X_XX",
                        "X__X",
                        "X__X")),
        O(
                'O',
                Arrays.asList(
                        "XXX",
                        "X_X",
                        "X_X",
                        "X_X",
                        "XXX")),
        P(
                'P',
                Arrays.asList(
                        "XXX",
                        "X_X",
                        "XXX",
                        "X__",
                        "X__")),
        Q(
                'Q',
                Arrays.asList(
                        "XXX",
                        "X_X",
                        "X_X",
                        "XX_",
                        "__X")),
        R(
                'R',
                Arrays.asList(
                        "XXX",
                        "X_X",
                        "XX_",
                        "X_X",
                        "X_X")),
        S(
                'S',
                Arrays.asList(
                        "XXX",
                        "X__",
                        "XXX",
                        "__X",
                        "XXX")),
        T(
                'T',
                Arrays.asList(
                        "XXX",
                        "_X_",
                        "_X_",
                        "_X_",
                        "_X_")),
        U(
                'U',
                Arrays.asList(
                        "X_X",
                        "X_X",
                        "X_X",
                        "X_X",
                        "XXX")),
        V(
                'V',
                Arrays.asList(
                        "X_X",
                        "X_X",
                        "X_X",
                        "X_X",
                        "_X_")),
        W(
                'W',
                Arrays.asList(
                        "X_X",
                        "X_X",
                        "XXX",
                        "XXX",
                        "X_X")),
        X(
                'X',
                Arrays.asList(
                        "X_X",
                        "X_X",
                        "_X_",
                        "X_X",
                        "X_X")),
        Y(
                'Y',
                Arrays.asList(
                        "X_X",
                        "X_X",
                        "_X_",
                        "_X_",
                        "_X_")),
        Z(
                'Z',
                Arrays.asList(
                        "XXX",
                        "__X",
                        "_X_",
                        "X__",
                        "XXX")),
        SPACE(
                ' ',
                Arrays.asList(
                        "_",
                        "_",
                        "_",
                        "_",
                        "_")),
        COLON(
                ':',
                Arrays.asList(
                        "_",
                        "X",
                        "_",
                        "X",
                        "_")),
        RIGHT_PARENTHESIS(
                ')',
                Arrays.asList(
                        "X_",
                        "_X",
                        "_X",
                        "_X",
                        "X_"));

        private char letter;
        private List<String> lines = new ArrayList<>();
        private HashMap<Coordinate, ItemCreator> coordinates = new HashMap<>();
        private ItemCreator itemCreator;

        Letter(char letter, List<String> lines) {
            this.letter = letter;
            this.lines = lines;
            this.coordinates = draw();
        }

        Letter(char letter, List<String> lines, ItemCreator itemCreator) {
            this.letter = letter;
            this.lines = lines;
            this.itemCreator = itemCreator;
            this.coordinates = draw();
        }

        public static Letter getLetter(char c) {
            for (Letter letter : values()) {
                if (letter.getLetter() == c) {
                    return letter;
                }
            }
            return A;
        }

        public static double getLength(String text) {
            double length = 0;
            for (char c : text.toCharArray()) {
                length = length + Letter.getLetter(c).getLength();
                if (c != ' ') {
                    length = length + 1;
                } else {
                    length = length + 2;
                }
            }
            return length;
        }

        public HashMap<Coordinate, ItemCreator> draw() {
            HashMap<Coordinate, ItemCreator> map = new HashMap<>();
            int currentX = 0;
            int currentY = 0;
            for (String line : getLines()) {
                for (char c : line.toCharArray()) {
                    Coordinate coordinate = new Coordinate(currentX, currentY);
                    ItemCreator itemCreator = c == '_' ? new ItemCreator(Material.AIR) : getItemCreator();
                    map.put(coordinate, itemCreator);
                    currentX++;
                }
                currentX = 0;
                currentY--;
            }
            return map;
        }

        public HashMap<Coordinate, ItemCreator> getCoordinates() {
            return this.coordinates;
        }

        public char getLetter() {
            return this.letter;
        }

        public List<String> getLines() {
            return this.lines;
        }

        public ItemCreator getItemCreator() {
            return this.itemCreator;
        }

        public double getHeight() {
            return getLines().size();
            /*double lowestPoint = 0;
            double highestPoint = 0;
            for (Coordinate coordinate : getCoordinates().keySet()) {
                if (coordinate.getY() < lowestPoint) {
                    lowestPoint = coordinate.getY();
                } else if (coordinate.getY() > highestPoint) {
                    highestPoint = coordinate.getY();
                }
            }
            return Math.abs(highestPoint - lowestPoint) + 1;*/
        }

        public double getLength() {
            return getLines().get(0).length();
            /*double leftmostPoint = 0;
            double rightmostPoint = 0;
            for (Coordinate coordinate : getCoordinates().keySet()) {
                if (coordinate.getX() <= leftmostPoint) {
                    leftmostPoint = coordinate.getX();
                } else if (coordinate.getX() >= rightmostPoint) {
                    rightmostPoint = coordinate.getX();
                }
            }
            return Math.abs(leftmostPoint - rightmostPoint) + 1;*/
        }

    }

}
