package org.openlca.core.matrix.solvers;

public class UmfFactorizedMatrix {
    final long pointer;

    UmfFactorizedMatrix(long pointer) {
        this.pointer = pointer;
    }

    public void dispose() {
        Julia.umfDispose(pointer);
    }

}
