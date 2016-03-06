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
        if(compareObj instanceof IcaRecord){
            IcaRecord compareIca = (IcaRecord) compareObj;
            if ((this.getClass().getName() == compareObj.getClass().getName()) &&
                    this.getId() == compareIca.getId()){
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hashCode = 17;
        hashCode += 0 == getId() ? 0 : getId() * 31;
        return hashCode;
    }

}
