package data;

public class Inventory {
    private String InvDate;
    private String DrugCode;
    private String MakerID;
    private String StoreID;
    private String AreaNo;
    private String BlockNo;
    private String BlockType;
    private String LotNumber;
    private String StockQty;
    private String InventoryQty;
    private String AdjQty;
    private String ShiftNo;
    private String InvTime;
    private String UserID;
    private String Remark;

    public String getInvDate() {
        return InvDate;
    }

    public void setInvDate(String InvDate) {
        this.InvDate = InvDate;
    }

    public String getDrugCode() {
        return DrugCode;
    }

    public void setDrugCode(String DrugCode) {
        this.DrugCode = DrugCode;
    }

    public String getMakerID() {
        return MakerID;
    }

    public void setMakerID(String MakerID) {
        this.MakerID = MakerID;
    }

    public String getStoreID() {
        return StoreID;
    }

    public void setStoreID(String StoreID) {
        this.StoreID = StoreID;
    }

    public String getAreaNo() {
        return AreaNo;
    }

    public void setAreaNo(String AreaNo) {
        this.AreaNo = AreaNo;
    }

    public String getBlockNo() {
        return BlockNo;
    }

    public void setBlockNo(String BlockNo) {
        this.BlockNo = BlockNo;
    }

    public String getLotNumber() {
        return LotNumber;
    }

    public void setLotNumber(String LotNumber) {
        this.LotNumber = LotNumber;
    }

    public String getStockQty() {
        return StockQty;
    }

    public void setStockQty(String StockQty) {
        this.StockQty = StockQty;
    }

    public String getInventoryQty() {
        return InventoryQty;
    }

    public void setInventoryQty(String InventoryQty) {
        this.InventoryQty = InventoryQty;
    }

    public String getAdjQty() {
        return AdjQty;
    }

    public void setAdjQty(String AdjQty) {
        this.AdjQty = AdjQty;
    }

    public String getBlockType() {
        return BlockType;
    }

    public void setBlockType(String BlockType) {
        this.BlockType = BlockType;
    }

    public String getShiftNo() {
        return ShiftNo;
    }

    public void setShiftNo(String ShiftNo) {
        this.ShiftNo = ShiftNo;
    }

    public String getInvTime() {
        return InvTime;
    }

    public void setInvTime(String InvTime) {
        this.InvTime = InvTime;
    }

    public String getUserID() {
        return UserID;
    }

    public void setUserID(String UserID) {
        this.UserID = UserID;
    }

    public String getRemark() {
        return Remark;
    }

    public void setRemark(String Remark) {
        this.Remark = Remark;
    }

    public String getRowString(){return  getInvDate()+","+getDrugCode()+","+getMakerID()+","+getStoreID()+","+getAreaNo()+","+getBlockNo()
            +","+getBlockType()+","+getLotNumber()+","+getStockQty()+","+getInventoryQty()+","+getAdjQty()+","+getShiftNo()+","+getInvTime()
            +","+getUserID()+","+getRemark();}
}
