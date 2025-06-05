import { Repository } from 'typeorm';
import { AppDataSource } from '@/config/database.config';

export class PaymentService {
    //TODO:  Create a payment simulator, not use real payment gateways because there is no api secret to call.
    // It should return success or failure randomly with rate 70-30
}