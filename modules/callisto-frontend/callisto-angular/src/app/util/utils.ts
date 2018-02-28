export class Utils {
    constructor() {}
    public static checkNullEmpty(obj) {
        return (obj == null || obj == undefined || obj === '' || Object.keys(obj).length === 0);
    }
}