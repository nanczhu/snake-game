package com.gamecodeschool.snakegame;
class Main {
    public static void main(String[] args) {
        Person person1 = new Person("Barn", 11);
        Person person2 = new Person("Yara", 10);
        System.out.println("Name: " + person1.getName() + ", Age:" + person1.getAge());
        System.out.println("Name: " + person2.getName() + ", Age:" + person2.getAge());

        Player player1 = new Player(person1.getName(), person1.getAge(), "Junmo", 111);
        System.out.println("Name:" + player1.getName() + ", Age:" + player1.getAge()
                + ", Game Name:" + player1.getGameName() + ", Game Id: " + player1.getGameId());

        Player player2 = new Player(person2.getName(), person2.getAge(), "Barnnny", 222);
        System.out.println("Name:" + player2.getName() + ", Age:" + player2.getAge()
                + ", Game Name:" + player2.getGameName() + ", Game Id: " + player2.getGameId());

    }

}
