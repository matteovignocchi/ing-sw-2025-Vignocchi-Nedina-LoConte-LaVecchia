package it.polimi.ingsw.galaxytrucker.View;

import it.polimi.ingsw.galaxytrucker.Client.ClientTile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TileRenderer {
    private static final String RESET = "\u001B[0m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String BLUE = "\u001B[34m";
    private static final String PURPLE = "\u001B[35m";
    private static final String BROWN = "\u001B[33m";
    private static final String PEFOH = "\u001B[36m";

    public static String[] renderClientTile(ClientTile tile) {
        String[] out = new String[3];
        int a = tile.a, b = tile.b, c = tile.c, d = tile.d;
        String label = getLabel(tile);

        out[0] = String.format("    %d    ", a);
        out[1] = String.format("%d  %-4s %d", d, label, b);
        out[2] = String.format("    %d    ", c);

        return out;
    }

    private static String getLabel(ClientTile tile) {
        return switch (tile.type.toUpperCase()) {
            case "STORAGEUNIT" -> {
                String summary = summarizeGoods(tile.goods);
                yield tile.advance ? "S*A" : summary;
            }
            case "ENGINE" -> tile.idDouble ? "EnD" : "Eng";
            case "CANNON" -> tile.idDouble ? "CnD" : "Can";
            case "HOUSINGUNIT" -> {
                String summary = summarizeTokens(tile.tokens);
                yield summary.isEmpty() ? "Hous" : summary;
            }
            case "ENERGYCELL" -> "Ene";
            case "SHIELD" -> "Shi";
            case "MULTIJOINT" -> "Mlt";
            default -> tile.type.substring(0, Math.min(4, tile.type.length()));
        };
    }

    private static String summarizeGoods(List<String> goods) {
        // Esempio: "r1g2" = 1 red, 2 green
        Map<String, Long> counts = goods.stream()
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));
        return counts.entrySet().stream()
                .map(e -> e.getKey().charAt(0) + "" + e.getValue())
                .collect(Collectors.joining(""));
    }

    private static String summarizeTokens(List<String> tokens) {
        if (tokens.isEmpty()) return "";
        Map<String, Long> counts = tokens.stream()
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));
        return counts.entrySet().stream()
                .map(e -> e.getKey().substring(0, 2).toUpperCase())
                .collect(Collectors.joining());
    }

    private String[] renderTile(ClientTile tile) {
        String[] out = new String[3];
        int a = tile.a;
        int b = tile.b;
        int c = tile.c;
        int d = tile.d;
        String label = getTileContent(tile); // puoi lasciarlo com'è se lavora su `tile.type`

        switch (tile.type.toUpperCase()) {
            case "EMPTYSPACE" -> {
                out[0] = "         ";
                out[1] = "         ";
                out[2] = "         ";
            }
            case "ENGINE", "CANNON", "MULTIJOINT", "HOUSINGUNIT" -> {
                out[0] = String.format("    %d    ", a);
                out[1] = String.format("%d  %-4s %d", d, label, b);
                out[2] = String.format("    %d    ", c);
            }
            case "ENERGYCELL" -> {
                out[0] = String.format("    %d    ", a);
                out[1] = String.format("%d  %-4s  %d", d, label, b);
                out[2] = String.format("    %d    ", c);
            }
            case "SHIELD" -> {
                List<Integer> pc = tile.protectedCorners != null ? tile.protectedCorners : List.of(0, 0, 0, 0);
                if (pc.get(0) == 8 && pc.get(1) == 8) {
                    out[0] = String.format("    %s%d%s    ", GREEN, a, RESET);
                    out[1] = String.format("%d  %-4s %s%d%s", d, label, GREEN, b, RESET);
                    out[2] = String.format("    %d    ", c);
                } else if (pc.get(1) == 8 && pc.get(2) == 8) {
                    out[0] = String.format("    %d    ", a);
                    out[1] = String.format("%d  %-4s %s%d%s", d, label, GREEN, b, RESET);
                    out[2] = String.format("    %s%d%s    ", GREEN, c, RESET);
                } else if (pc.get(2) == 8 && pc.get(3) == 8) {
                    out[0] = String.format("    %d    ", a);
                    out[1] = String.format("%s%d%s  %-4s %d", GREEN, d, RESET, label, b);
                    out[2] = String.format("    %s%d%s    ", GREEN, c, RESET);
                } else if (pc.get(3) == 8 && pc.get(0) == 8) {
                    out[0] = String.format("    %s%d%s    ", GREEN, a, RESET);
                    out[1] = String.format("%s%d%s  %-4s %d", GREEN, d, RESET, label, b);
                    out[2] = String.format("    %d    ", c);
                } else {
                    out[0] = String.format("    %d    ", a);
                    out[1] = String.format("%d  %-4s %d", d, label, b);
                    out[2] = String.format("    %d    ", c);
                }
            }
            case "STORAGEUNIT" -> {
                List<String> goods = tile.goods != null ? tile.goods : new ArrayList<>();
                int red = 0, green = 0, yellow = 0, blue = 0;
                for (String g : goods) {
                    switch (g.toUpperCase()) {
                        case "RED" -> red++;
                        case "GREEN" -> green++;
                        case "YELLOW" -> yellow++;
                        case "BLUE" -> blue++;
                    }
                }
                out[0] = String.format("%s%d%s   %d   %s%d%s", RED, red, RESET, a, YELLOW, yellow, RESET);
                out[1] = String.format("%d  %-4s  %d", d, label, b);
                out[2] = String.format("%s%d%s   %d   %s%d%s", BLUE, blue, RESET, c, GREEN, green, RESET);
            }
            default -> {
                out[0] = "         ";
                out[1] = String.format("%d  %-4s %d", d, label, b);
                out[2] = "         ";
            }
        }
        return out;
    }
    private String getTileContent(ClientTile tile) {
        return switch (tile.type.toUpperCase()) {
            case "ENGINE" -> tile.idDouble ? "EnD" : "Eng";
            case "CANNON" -> tile.idDouble ? "CnD" : "Can";
            case "ENERGYCELL" -> "Ene";
            case "MULTIJOINT" -> "Mlt";
            case "STORAGEUNIT" -> tile.advance ? "S*A" : "Stor";
            case "HOUSINGUNIT" -> "Hous";
            case "SHIELD" -> "Shi";
            default -> "???";
        };
    }


}
