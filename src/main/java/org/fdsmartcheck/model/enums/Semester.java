package org.fdsmartcheck.model.enums;

public enum Semester {
    S1(1),
    S2(2),
    S3(3),
    S4(4),
    S5(5),
    S6(6),
    S7(7),
    S8(8),
    S9(9),
    S10(10),
    S11(11),
    S12(12),
    S13(13),
    S14(14);

    private final int number;

    Semester(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public static Semester fromNumber(int number) {
        for (Semester semester : values()) {
            if (semester.number == number) {
                return semester;
            }
        }
        throw new IllegalArgumentException("Semestre inválido: " + number + ". Use um valor entre 1 e 14.");
    }
}
