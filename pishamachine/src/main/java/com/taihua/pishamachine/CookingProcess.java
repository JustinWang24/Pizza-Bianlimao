package com.taihua.pishamachine;

/**
 * Created by Justin Wang from SmartBro on 31/12/17.
 */

public class CookingProcess {
    private boolean makePizza;
    private boolean isTaken;
    private boolean isResetting;
    private int setDoneActionValue;

    public CookingProcess(){
        this.makePizza = true;
        this.isTaken = false;
        this.setDoneActionValue = MachineStatusOfMakingPizza.MACHINE_RESET_ERROR;
        this.isResetting = false;
    }

    /**
     * 判断是否正在烤饼过程中:
     * @return
     */
    public boolean isMaking(){
        return this.makePizza && !this.isTaken && !this.isResetting;
    }

    /**
     * 判断是否正在等待客户取饼
     * @return
     */
    public boolean waitingForTaken(){
        return !this.makePizza && !this.isTaken && !this.isResetting;
    }

    /**
     * 判断是否已经烤完了
     * @return
     */
    public boolean complete(){
        return !this.makePizza && this.isTaken && !this.isResetting;
    }

    public void reset(){
        this.makePizza = true;
        this.isTaken = false;
        this.isResetting = false;
        this.setDoneActionValue = MachineStatusOfMakingPizza.MACHINE_RESET_ERROR;
    }

    public void updateSetDoneActionValue(int value){
        this.setDoneActionValue = value;
    }

    public int getSetDoneActionValue(){
        return this.setDoneActionValue;
    }

    /**
     * 转入等待客户取饼的状态
     */
    public void switchToWaitingForTakenStage(){
        this.makePizza = false;
        this.isTaken = false;
    }

    /**
     * 转换到等待PLC复位的状态
     */
    public void switchToWaitingForResetStage(){
        this.isResetting = true;
    }

    /**
     * 判断是否处于等待PLC复位的过程中
     * @return
     */
    public boolean isWaitingForPlcResetStage(){
        return this.isResetting;
    }

    /**
     * 转入到饼已经烤完的状态
     */
    public void switchToCompleteStage(){
        this.makePizza = false;
        this.isTaken = true;
        this.isResetting = false;
    }
}
