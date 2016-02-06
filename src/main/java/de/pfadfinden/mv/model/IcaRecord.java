package de.pfadfinden.mv.model;

public abstract class IcaRecord {

    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object compareObj){
        if(compareObj.getClass().isInstance(IcaRecord.class)){
            IcaRecord compareIca = (IcaRecord) compareObj;
            if ((this.getClass().getName() == compareObj.getClass().getName()) &&
                    this.getId() == compareIca.getId()){
                return true;
            }
        }

        return false;
    }
}
