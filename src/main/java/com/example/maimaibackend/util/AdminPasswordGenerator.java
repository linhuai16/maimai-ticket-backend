package com.example.maimaibackend.util;

/**
 * 在 IDEA 中直接运行 main 方法，可以生成可写入 admin_account.password_hash 的 PBKDF2 密码。
 */
public final class AdminPasswordGenerator {

    private AdminPasswordGenerator() {
    }

    public static void main(String[] args) {
        if (args.length != 1 || args[0] == null || args[0].isEmpty()) {
            System.out.println("用法：运行时传入一个密码参数，例如 Admin@123456");
            return;
        }
        System.out.println(PasswordUtil.encode(args[0]));
    }
}
