export class Utils {
    constructor() {}
    public static checkNullEmpty(obj) : boolean {
        return (obj == null || obj == undefined || obj === '' || Object.keys(obj).length === 0);
    }

    public static checkNotNullEmpty(obj) : boolean {
        return !Utils.checkNullEmpty(obj);
    }
}