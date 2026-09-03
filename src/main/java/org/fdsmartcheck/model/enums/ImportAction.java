package org.fdsmartcheck.model.enums;

public enum ImportAction {
    /** Aluno novo: será criado */
    CREATE,
    /** RA já cadastrado: nome e turma serão atualizados */
    UPDATE,
    /** Não será importado */
    SKIP
}
