export type FormFields<T extends object> = {
  [K in keyof T]: string;
};